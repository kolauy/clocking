package com.genisky.server;

import com.genisky.account.AuditItemInfo;
import com.genisky.account.AuthenticationResponse;
import com.genisky.account.MessageInfo;
import com.genisky.account.OfficeInfo;
import com.genisky.account.OrganizationInfo;
import com.genisky.account.PrepareResponse;
import com.genisky.account.ReportInfo;
import com.genisky.account.UserInfo;

public class GeniskyServices extends ServerConnection{
    private final AuthenticationResponse _account;

    public GeniskyServices(AuthenticationResponse account){
        super(account.token, account.services);
        _account = account;
    }

    public AuthenticationResponse getAccount(){
        return _account;
    }

    public UserInfo getUserInfo() {
        return sendGet("/userinfo/" + _account.id,  UserInfo.class);
    }

    public String getUserState() {
        return sendGet("/userstate/" + _account.id, String.class);
    }

    public OrganizationInfo getOrganization(){
        return sendGet("/organizationinfo/" + _account.id, OrganizationInfo.class);
    }

    public OfficeInfo getOffice(String company){
        return sendGet("/officeinfo/" + company, OfficeInfo.class);
    }

    public MessageInfo getUnreadMessages(){
        return sendGet("/messageinfo/" + _account.id, MessageInfo.class);
    }

    public AuditItemInfo getAuditItems() {
        return sendGet("/audititeminfo/" + _account.id, AuditItemInfo.class);
    }

    public ReportInfo getReportInfo(String start, String stop) {
        return sendGet("/reportinfo/" + _account.id + "/" + start + "/" + stop, ReportInfo.class);
    }

    public PrepareResponse prepareClocking(String day) {
        return sendGet("/prepareclocking/" + _account.id + "/" + day, PrepareResponse.class);
    }

    public PrepareResponse  prepareAmending(){
        return sendGet("/prepareamending/" + _account.id, PrepareResponse.class);
    }

    public PrepareResponse amending(int[] people, String date){
        AmendingRequest request = new AmendingRequest();
        request.request = _account.id;
        request.people = people;
        request.date = date;
        String entity = _gson.toJson(request);
        return sendPost("/amending", entity, PrepareResponse.class);
    }

    public PrepareResponse prepareAdjusting() {
        return sendGet("/prepareadjusting/" + _account.id, PrepareResponse.class);
    }

    public PrepareResponse clocking(String code, String date, String time, double longitude, double latitude, String office){
        ClockingRequest request = new ClockingRequest();
        request.request = _account.id;
        request.code = code;
        request.date = date;
        request.time = time;
        request.longitude = longitude;
        request.latitude = latitude;
        request.office = office;
        String entity = _gson.toJson(request);
        return sendPost("/clocking", entity, PrepareResponse.class);
    }

    public PrepareResponse adjusting(int[] people, String date, int audit){
        AdjustingRequest request = new AdjustingRequest();
        request.request = _account.id;
        request.people = people;
        request.date = date;
        request.audit = audit;
        String entity = _gson.toJson(request);
        return sendPost("/adjusting", entity, PrepareResponse.class);
    }

    public PrepareResponse overworking(int[] people, String date, String starttime, String stoptime, int audit) {
        OverworkingRequest request = new OverworkingRequest();
        request.request = _account.id;
        request.people = people;
        request.date = date;
        request.starttime = starttime;
        request.stoptime = stoptime;
        request.audit = audit;
        String entity = _gson.toJson(request);
        return sendPost("/overworking", entity, PrepareResponse.class);
    }

    public PrepareResponse auditing(int id, boolean accept) {
        AuditingRequest request = new AuditingRequest();
        request.request = _account.id;
        request.id = id;
        request.operation = accept ? "accept" : "reject";
        return sendPost("/auditing", _gson.toJson(request), PrepareResponse.class);
    }

    class ClockingRequest{
        public int request;
        public String code;
        public String date;
        public String time;
        public double longitude;
        public double latitude;
        public String office;
    }

    class AmendingRequest{
        public int request;
        public int[] people;
        public String date;
    }

    class AdjustingRequest{
        public int request;
        public int[] people;
        public String date;
        public int audit;
    }

    class OverworkingRequest{
        public int request;
        public int[] people;
        public String date;
        public String starttime;
        public String stoptime;
        public int audit;
    }

    private class AuditingRequest {
        public int request;
        public int id;
        public String operation;
    }
}

