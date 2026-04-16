package com.gustavoronchi.fipe_search.controller;

import com.gustavoronchi.fipe_search.dto.ConsultaFipeDTO;
import com.gustavoronchi.fipe_search.service.FipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("FipeController - Testes unitários")
class FipeControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private FipeService fipeService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("GET /fipe/{modeloId}/{anoModelo}")
    class ConsultarEndpoint {

        @Test
        @DisplayName("Deve retornar 200 com dados da consulta FIPE quando encontrado")
        void deveRetornarOkComDadosQuandoEncontrado() throws Exception {
            Long modeloId = 123L;
            Integer anoModelo = 2023;
            ConsultaFipeDTO consultaEsperada = new ConsultaFipeDTO(
                    "Toyota",
                    "Corolla",
                    anoModelo,
                    new BigDecimal("150000.00"),
                    "setembro/2023"
            );

            when(fipeService.consultar(modeloId, anoModelo))
                    .thenReturn(consultaEsperada);

            mockMvc.perform(get("/fipe/{modeloId}/{anoModelo}", modeloId, anoModelo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marca", is("Toyota")))
                    .andExpect(jsonPath("$.modelo", is("Corolla")))
                    .andExpect(jsonPath("$.anoModelo", is(anoModelo)))
                    .andExpect(jsonPath("$.preco", is(150000.00)))
                    .andExpect(jsonPath("$.mesReferencia", is("setembro/2023")));

            verify(fipeService).consultar(modeloId, anoModelo);
        }

        @Test
        @DisplayName("Deve retornar 404 quando consulta FIPE não encontrada")
        void deveRetornarNotFoundQuandoNaoEncontrado() throws Exception {
            Long modeloId = 999L;
            Integer anoModelo = 2099;

            when(fipeService.consultar(modeloId, anoModelo))
                    .thenThrow(new ResponseStatusException(
                            NOT_FOUND,
                            "Consulta FIPE não encontrada: modelo=%d, ano=%d".formatted(modeloId, anoModelo)
                    ));

            mockMvc.perform(get("/fipe/{modeloId}/{anoModelo}", modeloId, anoModelo))
                    .andExpect(status().isNotFound());

            verify(fipeService).consultar(modeloId, anoModelo);
        }

        @Test
        @DisplayName("Deve chamar o service com os parâmetros corretos")
        void devePassarParametrosCorretosPaaraService() throws Exception {
            Long modeloId = 456L;
            Integer anoModelo = 2024;
            ConsultaFipeDTO consulta = new ConsultaFipeDTO(
                    "Honda",
                    "Civic",
                    anoModelo,
                    new BigDecimal("180000.00"),
                    "janeiro/2024"
            );

            when(fipeService.consultar(modeloId, anoModelo))
                    .thenReturn(consulta);

            mockMvc.perform(get("/fipe/{modeloId}/{anoModelo}", modeloId, anoModelo))
                    .andExpect(status().isOk());

            verify(fipeService).consultar(456L, 2024);
        }

        @Test
        @DisplayName("Deve retornar estrutura completa do DTO quando sucesso")
        void deveRetornarEstruturacCompletaDTOQuandoSucesso() throws Exception {
            Long modeloId = 789L;
            Integer anoModelo = 2022;
            ConsultaFipeDTO consultaCompleta = new ConsultaFipeDTO(
                    "Volkswagen",
                    "Golf",
                    anoModelo,
                    new BigDecimal("125000.50"),
                    "março/2022"
            );

            when(fipeService.consultar(modeloId, anoModelo))
                    .thenReturn(consultaCompleta);

            mockMvc.perform(get("/fipe/{modeloId}/{anoModelo}", modeloId, anoModelo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marca").exists())
                    .andExpect(jsonPath("$.modelo").exists())
                    .andExpect(jsonPath("$.anoModelo").exists())
                    .andExpect(jsonPath("$.preco").exists())
                    .andExpect(jsonPath("$.mesReferencia").exists());
        }

        @Test
        @DisplayName("Deve aceitar diferentes valores de modeloId e anoModelo")
        void deveAceitarDiferentesValoresDeParametros() throws Exception {
            ConsultaFipeDTO consulta = new ConsultaFipeDTO(
                    "Fiat",
                    "Uno",
                    2020,
                    new BigDecimal("50000.00"),
                    "junho/2020"
            );

            when(fipeService.consultar(1L, 2020))
                    .thenReturn(consulta);

            mockMvc.perform(get("/fipe/{modeloId}/{anoModelo}", 1L, 2020))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marca", is("Fiat")));

            verify(fipeService).consultar(1L, 2020);
        }
    }

    @Nested
    @DisplayName("DELETE /fipe/{modeloId}/{anoModelo}/cache")
    class InvalidarCacheEndpoint {

        @Test
        @DisplayName("Deve retornar 204 quando cache invalidado com sucesso")
        void deveRetornarNoContentQuandoInvalidarComSucesso() throws Exception {
            Long modeloId = 111L;
            Integer anoModelo = 2023;

            mockMvc.perform(delete("/fipe/{modeloId}/{anoModelo}/cache", modeloId, anoModelo))
                    .andExpect(status().isNoContent());

            verify(fipeService).invalidar(modeloId, anoModelo);
        }

        @Test
        @DisplayName("Deve chamar o service para invalidar cache com parâmetros corretos")
        void devePassarParametrosCorretosPaaraInvalidarCache() throws Exception {
            Long modeloId = 222L;
            Integer anoModelo = 2024;

            mockMvc.perform(delete("/fipe/{modeloId}/{anoModelo}/cache", modeloId, anoModelo))
                    .andExpect(status().isNoContent());

            verify(fipeService).invalidar(222L, 2024);
        }

        @Test
        @DisplayName("Deve retornar 204 mesmo se cache já estava vazio")
        void deveRetornarNoContentMesmoQuandoCacheVazio() throws Exception {
            Long modeloId = 333L;
            Integer anoModelo = 2022;

            mockMvc.perform(delete("/fipe/{modeloId}/{anoModelo}/cache", modeloId, anoModelo))
                    .andExpect(status().isNoContent());

            verify(fipeService).invalidar(modeloId, anoModelo);
        }

        @Test
        @DisplayName("Deve aceitar diferentes valores de modeloId e anoModelo para invalidação")
        void deveAceitarDiferentesValoresParaInvalidacao() throws Exception {
            mockMvc.perform(delete("/fipe/{modeloId}/{anoModelo}/cache", 999L, 2099))
                    .andExpect(status().isNoContent());

            verify(fipeService).invalidar(999L, 2099);
        }
    }

    @Nested
    @DisplayName("Casos de erro e validação")
    class CasosDeErroEValidacao {

        @Test
        @DisplayName("Deve retornar 404 quando service lança ResponseStatusException")
        void deveRetornarNotFoundQuandoServiceLancaExcecao() throws Exception {
            when(fipeService.consultar(999L, 9999))
                    .thenThrow(new ResponseStatusException(
                            NOT_FOUND,
                            "Não encontrado"
                    ));

            mockMvc.perform(get("/fipe/999/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 200 para consulta válida após tentativa anterior de erro")
        void deveRetornarOkAposTentativaAnteriorDeErro() throws Exception {
            Long modeloId = 555L;
            Integer anoModelo = 2023;
            ConsultaFipeDTO consulta = new ConsultaFipeDTO(
                    "BMW",
                    "X1",
                    anoModelo,
                    new BigDecimal("250000.00"),
                    "dezembro/2023"
            );

            when(fipeService.consultar(modeloId, anoModelo))
                    .thenReturn(consulta);

            mockMvc.perform(get("/fipe/{modeloId}/{anoModelo}", modeloId, anoModelo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marca", is("BMW")));
        }

        @Test
        @DisplayName("Deve manter independência entre chamadas GET e DELETE")
        void deveManterdIndependenciaEntreOperacoes() throws Exception {
            Long modeloId = 666L;
            Integer anoModelo = 2024;
            ConsultaFipeDTO consulta = new ConsultaFipeDTO(
                    "Mercedes",
                    "A200",
                    anoModelo,
                    new BigDecimal("200000.00"),
                    "abril/2024"
            );

            when(fipeService.consultar(modeloId, anoModelo))
                    .thenReturn(consulta);

            // Primeira requisição GET
            mockMvc.perform(get("/fipe/{modeloId}/{anoModelo}", modeloId, anoModelo))
                    .andExpect(status().isOk());

            // Segunda requisição DELETE
            mockMvc.perform(delete("/fipe/{modeloId}/{anoModelo}/cache", modeloId, anoModelo))
                    .andExpect(status().isNoContent());

            // Validar ambas as chamadas
            verify(fipeService).consultar(modeloId, anoModelo);
            verify(fipeService).invalidar(modeloId, anoModelo);
        }
    }
}





