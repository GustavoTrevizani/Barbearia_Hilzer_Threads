# 💈 Barbearia de Hilzer com Threads em Java

Este projeto implementa a solução do problema clássico da Barbearia de Hilzer utilizando programação concorrente em Java.

## 🧠 Objetivo

Simular o funcionamento de uma barbearia com múltiplos barbeiros e clientes concorrentes, garantindo sincronização correta entre threads e controle de recursos limitados.

## ⚙️ Requisitos do Problema

- 3 barbeiros
- 3 cadeiras de atendimento
- Sofá com 4 lugares
- Capacidade total de 20 clientes
- Fila de espera em pé
- Atendimento em ordem FIFO
- Promoção da fila em pé → sofá
- Apenas um cliente pode pagar por vez

## 🧵 Tecnologias e Conceitos Utilizados

- Threads em Java
- ReentrantLock (com fairness)
- Condition (await/signal)
- Semaphore (controle de pagamento)
- Estruturas FIFO (Queue)

## 🔄 Funcionamento

1. Clientes entram na barbearia (se houver capacidade)
2. Sentam no sofá ou aguardam em pé
3. Barbeiros chamam clientes do sofá (FIFO)
4. Clientes em pé são promovidos para o sofá
5. Após o corte, cliente realiza pagamento
6. Sistema garante exclusão mútua e sincronização correta

## ✅ Garantias da Solução

- Ausência de deadlock
- Ausência de starvation
- Sem busy-wait
- Pagamento serializado
- Ordem FIFO respeitada
- Sincronização correta entre cliente e barbeiro

## 📊 Logs

O sistema gera logs detalhados para auditoria, incluindo:

- Entrada de clientes
- Movimentação nas filas
- Atendimento pelos barbeiros
- Pagamento
- Saída

## 🚀 Como Executar

```bash
javac BarbeariaHilzer.java
java BarbeariaHilzer
