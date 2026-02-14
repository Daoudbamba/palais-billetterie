package com.palais.billetterie.scan.dto;

import com.palais.billetterie.ticket.domain.TicketStatus;

public class ScanResponse {
    public boolean valid;
    public String message;
    public TicketStatus status;
    public String eventTitle;
    public String userName;
    public String code;

    public static ScanResponse of(boolean valid, String message, TicketStatus status, String eventTitle, String userName, String code) {
        ScanResponse r = new ScanResponse();
        r.valid = valid;
        r.message = message;
        r.status = status;
        r.eventTitle = eventTitle;
        r.userName = userName;
        r.code = code;
        return r;
    }
}
