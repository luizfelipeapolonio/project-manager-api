package com.felipe.projectmanagerapi.exceptions;

import java.math.BigDecimal;

public class OutOfBudgetException extends RuntimeException {
  public OutOfBudgetException(BigDecimal budget, BigDecimal cost) {
    super(
      "Operação inválida! Custo acima do orçamento do projeto.\n" +
      "Orçamento: R$ " + budget + "\n" +
      "Custo: R$ " + cost
    );
  }
}
