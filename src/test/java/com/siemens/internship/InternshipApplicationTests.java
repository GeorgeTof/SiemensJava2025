package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class InternshipApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ItemService itemService;

	private ObjectMapper objectMapper = new ObjectMapper();

	private Item sampleItem;
	private Item anotherItem;

	@BeforeEach
	void setUp() {
		sampleItem = new Item(1L, "Hammer", "A useful tool", "NEW", "tools@example.com");
		anotherItem = new Item(2L, "Screwdriver", "Flat-head type", "NEW", "hardware@example.com");
	}

	@Test
	void testCreateItemSuccess() throws Exception {
		Mockito.when(itemService.save(Mockito.any())).thenReturn(sampleItem);

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleItem)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email", is("tools@example.com")));
	}

	@Test
	void testGetAllItems() throws Exception {
		Mockito.when(itemService.findAll()).thenReturn(Arrays.asList(sampleItem));

		mockMvc.perform(get("/api/items"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name", is("Hammer")));
	}

	@Test
	void testGetItemByIdFound() throws Exception {
		Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));

		mockMvc.perform(get("/api/items/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Hammer")));
	}

	@Test
	void testGetItemByIdNotFound() throws Exception {
		Mockito.when(itemService.findById(66L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/items/66"))
				.andExpect(status().isNotFound());
	}

	@Test
	void testUpdateItemFound() throws Exception {
		Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));
		Mockito.when(itemService.save(Mockito.any())).thenReturn(sampleItem);

		mockMvc.perform(put("/api/items/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleItem)))
				.andExpect(status().isAccepted());
	}

	@Test
	void testUpdateItemNotFound() throws Exception {
		Mockito.when(itemService.findById(87L)).thenReturn(Optional.empty());

		mockMvc.perform(put("/api/items/87")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleItem)))
				.andExpect(status().isNotFound());
	}

	@Test
	void testDeleteItem() throws Exception {
		mockMvc.perform(delete("/api/items/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	void testProcessItems() throws Exception {
		Mockito.when(itemService.processItemsAsync())
				.thenReturn(CompletableFuture.completedFuture(Arrays.asList(sampleItem, anotherItem)));

		mockMvc.perform(get("/api/items/process"))
				.andExpect(status().isOk());

		List<Item> processedItems = itemService.findAll();
		for (Item item : processedItems) {
			assertEquals("PROCESSED", item.getStatus());
		}
	}
}
