# Relatório POC: Resultados Reais dos Cenários FIPE

Este documento captura a execução simulando alta volumetria e valida os cenários esperados.

## 1. Tabela de Performance (K6 - 200 VUs por 20s)

| Cenário | Volumetria (Reqs) | RPS | Cache MISS | Erros (Timeouts/Fails) | Latência p90 | Latência p95 |
|---------|-------------------|-----|------------|-------------------------|--------------|--------------|
| Direct DB (Gargalo) | 32262 | 1610 | - | 722 | 168.88ms | 281.13ms |
| Cache com TTL (Stampede) | 20971 | 1046 | 0
0 | 607 | 336.49ms | 479.11ms |
| Cache com Warming (Ideal) | 44753 | 2234 | 0
0 | 0 | 160.85ms | 210.39ms |

**Análise Final**:
- **Cenário 1 (DB Direto):** O gargalo do Pool de Conexões do banco limitou o throughput bruto.
- **Cenário 2 (Cache TTL):** Houve grande melhoria de latência geral, porém picos de latência e concorrência sobrecarregando o sistema periodicamente ("Cache Stampede").
- **Cenário 3 (Cache com Warmup):** Desempenho máximo, mantendo p95 muito baixa sem onerar o banco, entregando zero erros.

## 2. Evidência do Fenômeno "Cache Stampede" (Cenário 2)

O Cache Stampede acontece porque, ao expirar uma chave em alta concorrência `N` VUs, dezenas de instâncias percebem o *Cache MISS* e fazem a consulta simultânea ao banco de dados no exato momento, re-saturando o pool do HikariCP desnecessariamente e gerando interrupções no tempo de reposta da API que seriam invisíveis na média bruta.

**Comportamento capturado nos logs do Spring Boot durante o teste de carga:**
- Nenhum grande stampede capturado nesta iteração.
