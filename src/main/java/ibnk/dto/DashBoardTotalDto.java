package ibnk.dto;

import lombok.Data;

@Data
public class DashBoardTotalDto {
    int totalPending;
    int totalRejected;
    int totalApproved;
    int subActive;
    int subInActive;
    int subPending;
    int subBlocked;
    int subSuspended;

}
