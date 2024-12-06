package ibnk.tools.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class GlobalResponse <K,V> {

    private K Dao;
    private V Message;



    public GlobalResponse(K Dao, V message){
        this.Dao = Dao;
        this.Message =  message;

    }
}
