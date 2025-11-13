package com.example.assistant;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@SpringBootApplication
public class AssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }

    @Bean
    McpSyncClient schedulerMcp() {
        var mcp = McpClient
                .sync(HttpClientSseClientTransport.builder("http://localhost:8081/").build())
                .build();
        mcp.initialize();
        return mcp;
    }

    @Bean
    PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
        var jdbc = JdbcChatMemoryRepository
                .builder()
                .dataSource(dataSource)
                .build();
        var mwa = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(jdbc)
                .build();
        return PromptChatMemoryAdvisor
                .builder(mwa)
                .build();
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return new QuestionAnswerAdvisor(vectorStore);
    }

    @Bean
    CommandLineRunner initData(DogRepository dogRepository, JdbcTemplate jdbcTemplate) {
        return args -> {
            // 确保表存在
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS dog (
                        id INTEGER PRIMARY KEY,
                        name VARCHAR(255),
                        owner VARCHAR(255),
                        description TEXT
                    )
                    """);

            // 检查是否已有数据，避免重复插入
            if (dogRepository.findAll().isEmpty()) {
                // 插入示例数据
                var dogs = List.of(
                        new Dog(1, "Buddy", "Available", "A friendly Golden Retriever, 3 years old, loves playing fetch and is great with children. Located in San Francisco."),
                        new Dog(2, "Luna", "Available", "A gentle Beagle mix, 2 years old, very calm and well-trained. Perfect for families. Located in London."),
                        new Dog(3, "Max", "Available", "An energetic Border Collie, 4 years old, loves outdoor activities and needs an active owner. Located in Tokyo."),
                        new Dog(4, "Bella", "Available", "A sweet Labrador, 1 year old, very playful and friendly. Great with other pets. Located in Paris."),
                        new Dog(5, "Charlie", "Available", "A loyal German Shepherd, 5 years old, well-trained and protective. Perfect for experienced owners. Located in Seoul.")
                );

                dogs.forEach(dog -> {
                    jdbcTemplate.update(
                            "INSERT INTO dog (id, name, owner, description) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                            dog.id(), dog.name(), dog.owner(), dog.description()
                    );
                });

                System.out.println("Inserted " + dogs.size() + " dog records into database");
            }
        };
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

// look mom, no Lombok!
record Dog(@Id int id, String name, String owner, String description) {
}

@Controller
@ResponseBody
class AssistantController {

    private final ChatClient ai;

    AssistantController(
            DogRepository repository,
            McpSyncClient schedulerMcp,
            VectorStore vectorStore, QuestionAnswerAdvisor questionAnswerAdvisor,
            PromptChatMemoryAdvisor promptChatMemoryAdvisor,
            ChatClient.Builder ai) {

        repository.findAll().forEach(dog -> {
            var dogument = new Document("id: %s, name: %s, description: %s"
                    .formatted(dog.id(), dog.name(), dog.description()));
            vectorStore.add(List.of(dogument));
        });

        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Malmo, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        this.ai = ai
                .defaultAdvisors(promptChatMemoryAdvisor, questionAnswerAdvisor)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(schedulerMcp))
                .defaultSystem(system)
                .build();
    }

    @GetMapping("/{user}/ask")
    String ask(@PathVariable String user, @RequestParam String question) {
        return this.ai
                .prompt(question)
                .advisors(p -> p.param(ChatMemory.CONVERSATION_ID, user))
                .call()
                .content();
        //.entity(DogAdoptionSuggestion.class);
    }
}

record DogAdoptionSuggestion(int id, String name) {
}
