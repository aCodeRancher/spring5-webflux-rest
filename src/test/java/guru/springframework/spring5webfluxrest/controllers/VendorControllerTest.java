package guru.springframework.spring5webfluxrest.controllers;

import guru.springframework.spring5webfluxrest.domain.Vendor;
import guru.springframework.spring5webfluxrest.repositories.VendorRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.given;

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
    ArgumentCaptor<Vendor> captor;

    @Before
    public void setUp() throws Exception {
        vendorRepository = Mockito.mock(VendorRepository.class);
        controller = new VendorController(vendorRepository);
        webTestClient = WebTestClient.bindToController(controller).build();
        captor = ArgumentCaptor.forClass(Vendor.class);
    }

    @Test
    public void list() {

        given(vendorRepository.findAll())
                .willReturn(Flux.just(Vendor.builder().firstName("Fred").lastName("Flintstone").build(),
                        Vendor.builder().firstName("Barney").lastName("Rubble").build()));

        webTestClient.get()
                .uri("/api/v1/vendors")
                .exchange()
                .expectBodyList(Vendor.class)
                .hasSize(2);
    }

    @Test
    public void getById() {
        given(vendorRepository.findById("someid"))
                .willReturn(Mono.just(Vendor.builder().firstName("Jimmy").lastName("Johns").build()));

        webTestClient.get()
                .uri("/api/v1/vendors/someid")
                .exchange()
                .expectBody(Vendor.class);
    }

    @Test
    public void testCreateVendor() {
        given(vendorRepository.saveAll(any(Publisher.class)))
                .willReturn(Flux.just(Vendor.builder().build()));

        Mono<Vendor> vendorToSaveMono = Mono.just(Vendor.builder().firstName("First Name")
                                                .lastName("Last Name").build());

        webTestClient.post()
                .uri("/api/v1/vendors")
                .body(vendorToSaveMono, Vendor.class)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    public void testUpdateVendor() {

       given(vendorRepository.save(any(Vendor.class)))
                .willReturn(Mono.just(Vendor.builder().build()));

        Mono<Vendor> vendorMonoToUpdate = Mono.just(Vendor.builder().build());

        webTestClient.put()
                .uri("/api/v1/vendors/someid")
                .body(vendorMonoToUpdate, Vendor.class)
                .exchange()
                 .expectStatus()
                .isOk();

    }

    @Test
    public void testPatchVendor() {

        Vendor vendor  = Vendor.builder().firstName("Oliver").build();
        Mono<Vendor> monoVendor = Mono.just(vendor);
        //request to patch this vendor
        Vendor vendor_patch  = Vendor.builder().firstName("Newton").build();
        Mono<Vendor> monoVendor_patch = Mono.just(vendor_patch);
        given(vendorRepository.findById("10")).willReturn(monoVendor);

        given(vendorRepository.save(any(Vendor.class)))
                .willReturn(monoVendor);
        webTestClient.patch()
                .uri("/api/v1/vendors/10")
                .body(monoVendor_patch, Vendor.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Vendor.class)
                .consumeWith(entityExchangeResult->{
                    assertTrue( entityExchangeResult.getResponseBody().getFirstName().equals("Newton")); });
        verify(vendorRepository,Mockito.times(1))
                .save(captor.capture());
    }

    @Test
    public void testPatchVendor_1() {

        Vendor vendor  = Vendor.builder().firstName("Oliver").build();
        Mono<Vendor> monoVendor = Mono.just(vendor);
        //request to patch this vendor
        Vendor vendor_patch  = Vendor.builder().firstName("Oliver").build();
        Mono<Vendor> monoVendor_patch = Mono.just(vendor_patch);
        given(vendorRepository.findById("10")).willReturn(monoVendor);

        given(vendorRepository.save(any(Vendor.class)))
                .willReturn(monoVendor);
        webTestClient.patch()
                .uri("/api/v1/vendors/10")
                .body(monoVendor_patch, Vendor.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Vendor.class)
                .consumeWith(entityExchangeResult->{
                    assertTrue( entityExchangeResult.getResponseBody().getFirstName().equals("Oliver")); });
        verify(vendorRepository,Mockito.times(0))
                .save(captor.capture());
    }
}