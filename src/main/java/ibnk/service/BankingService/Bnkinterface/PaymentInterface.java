package ibnk.service.BankingService.Bnkinterface;

import ibnk.dto.BankingDto.PaymentDto;
import ibnk.dto.BankingDto.TransferModel.ExecutePayment;
import ibnk.dto.BankingDto.TransferModel.InitPayment;
import ibnk.dto.BankingDto.TransferModel.PayableResponse;
import ibnk.models.internet.enums.ChannelCode;
import jakarta.websocket.server.PathParam;

public interface PaymentInterface {
    //@PUT("/payment/{uuid}")
   PaymentDto executePayment(ExecutePayment body, @PathParam("uuid") String uuid);

    //@GET("/payment/{uuid}")
    PaymentDto getPayment(@PathParam("uuid") String uuid);
//    @GET("/transaction/{uuid}")
    PaymentDto transactionStatus(@PathParam("uuid") String uuid);
    //@POST("/payment")
    PaymentDto initiatePayment(InitPayment body);


    //@GET("/payable")
    PayableResponse searchpayable(ChannelCode channel, String billId);
}
