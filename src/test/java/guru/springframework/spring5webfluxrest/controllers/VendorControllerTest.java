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

        BDDMockito.given(vendorRepository.findAll())
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
        BDDMockito.given(vendorRepository.findById("someid"))
                .willReturn(Mono.just(Vendor.builder().firstName("Jimmy").lastName("Johns").build()));

        webTestClient.get()
                .uri("/api/v1/vendors/someid")
                .exchange()
                .expectBody(Vendor.class);
    }

    @Test
    public void testCreateVendor() {
        BDDMockito.given(vendorRepository.saveAll(any(Publisher.class)))
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
    public void testPutNewVendor(){
        Mono<Boolean> nonexist = Mono.just(false);
        Mono<Vendor> vendorToProvide = Mono.just(Vendor.builder().firstName("Helen").lastName("Ma").build());
        Mono<Vendor> vendorToSave = Mono.just(Vendor.builder().id("10").firstName("Helen").lastName("Ma").build());

        BDDMockito.given(vendorRepository.existsById("10")).willReturn(nonexist);

        BDDMockito.given(vendorRepository.save(any(Vendor.class)))
                .willReturn(vendorToSave);
           webTestClient.put()
                .uri("/api/v1/vendors/10")
                 .body(vendorToProvide, Vendor.class)
                .exchange()
                .expectStatus()
                .isOk();
        BDDMockito.verify(vendorRepository,Mockito.times(1)).save(vendorToSave.block());
    }

    @Test
    public void testPutExistingVendor(){
        Mono<Boolean>  exist = Mono.just(true);
        Mono<Vendor> vendorToProvide = Mono.just(Vendor.builder().id("20").firstName("Helen").lastName("Ma").build());
        BDDMockito.given(vendorRepository.existsById("20")).willReturn(exist);
        BDDMockito.given(vendorRepository.findById("20")).willReturn(vendorToProvide);
        BDDMockito.given(vendorRepository.save(any(Vendor.class)))
                .willReturn(vendorToProvide);
        webTestClient.put()
                .uri("/api/v1/vendors/20")
                .body(vendorToProvide, Vendor.class)
                .exchange()
                .expectStatus()
                .isOk();
        BDDMockito.verify(vendorRepository,Mockito.times(1)).save(vendorToProvide.block());
    }
}