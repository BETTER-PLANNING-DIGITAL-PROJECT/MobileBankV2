package ibnk.intergrations.Tranzak.requestDtos;

import lombok.Data;

@Data
public class InitiateCollection {
      String amount;
      String currencyCode;
      String description;
      String mchTransactionRef;
      String returnUrl;
}

