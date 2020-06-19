package guru.springframework.spring5webfluxrest.controllers;

import guru.springframework.spring5webfluxrest.domain.Vendor;
import guru.springframework.spring5webfluxrest.repositories.VendorRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class VendorControllerTest {

    WebTestClient webTestClient;
    VendorRepository vendorRepository;
    VendorController controller;

    @Before
    public void setUp() throws Exception {
        vendorRepository = Mockito.mock(VendorRepository.class);
        controller = new VendorController(vendorRepository);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    public void list() {
        Vendor vendor1 = Vendor.builder().firstName("Fred").lastName("Flintstone").build();
        Vendor vendor2 = Vendor.builder().firstName("Barney").lastName("Rubble").build();
        BDDMockito.given(vendorRepository.findAll())
                .willReturn(Flux.just(vendor1, vendor2));

        webTestClient.get()
                .uri("/api/v1/vendors")
                .exchange()
                .expectBodyList(Vendor.class)
                .contains(vendor1, vendor2)
                .hasSize(2);
    }

    @Test
    public void getById() {
        BDDMockito.given(vendorRepository.findById("someid"))
                .willReturn(Mono.just(Vendor.builder().firstName("Jimmy").lastName("Johns").build()));

        webTestClient.get()
                .uri("/api/v1/vendors/someid")
                .exchange()
                .expectBody(Vendor.class);
    }

    @Test
    public void testCreateVendor() {
        Vendor vendorToSave = Vendor.builder().firstName("Jim").lastName("Jims").build();
        Mono<Vendor>  monoOfVendor = Mono.just(vendorToSave);
        BDDMockito.given(vendorRepository.saveAll(any(Publisher.class)))
                .willReturn(Flux.just(vendorToSave));
        webTestClient.post()
                .uri("/api/v1/vendors")
                .body(monoOfVendor, Vendor.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Vendor.class);

    }

    @Test
    public void testPutVendor(){
        Vendor vendorToUpdate = Vendor.builder().id("10").firstName("Oliver").build();
        Mono<Vendor> vendorMono = Mono.just(vendorToUpdate);
        BDDMockito.given(vendorRepository.save(any(Vendor.class)))
                .willReturn(vendorMono);
        webTestClient.put()
                .uri("/api/v1/vendors/10")
                .body(vendorMono,Vendor.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Vendor.class)
                .consumeWith(entityExchangeResult ->
                  {
                      assertTrue(entityExchangeResult.getResponseBody().getFirstName().equals("Oliver"));
                      assertTrue(entityExchangeResult.getResponseBody().getId().equals("10"));
                  }
                  );

    }
}