package ibnk.tools.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class Tuple <T,K> {
    private T first;
    private K second;

    public Tuple(T first,K second){
        this.first = first;
        this.second = second;
    }
}
