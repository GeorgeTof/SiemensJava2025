package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     *
     * Explanation of problems and solutions:
     * Originally, the method returned a plain List immediately without waiting for any async tasks to finish.
     * It also used shared mutable fields (processedItems, processedCount) without synchronization, which is unsafe.
     * We now return a CompletableFuture and use async composition with supplyAsync and allOf, so we can wait
     * for everything to finish before returning. We also removed shared state and handle errors properly
     * inside the async tasks, including restoring the thread interrupt status.
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        // Process each item in a separate async task and collect futures
        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100); // simulating some work

                        // Fetch the item
                        Optional<Item> optionalItem = itemRepository.findById(id);
                        if (optionalItem.isEmpty()) {
                            return null;
                        }

                        // Update and save item
                        Item item = optionalItem.get();
                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                        throw new RuntimeException("Processing was interrupted", e);
                    } catch (Exception ex) {
                        throw new RuntimeException("Error processing item ID: " + id, ex); // Proper error propagation
                    }
                }))
                .toList();

        // Wait for all tasks to finish
        CompletableFuture<Void> allDone = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0])); // Barrier to join all futures

        // When all are done, collect results
        return allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join) // This is safe now because all are done
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
    }
}

