package eionet.datadict.model;

import java.util.Date;

public class GeneralLogEntry {

    private Date event_time;

    private String user_host;

    private int thread_id;

    private int server_id;

    private String command_type;

    private String argument;

    public Date getEvent_time() {
        return event_time;
    }

    public void setEvent_time(Date event_time) {
        this.event_time = event_time;
    }

    public String getUser_host() {
        return user_host;
    }

    public void setUser_host(String user_host) {
        this.user_host = user_host;
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setThread_id(int thread_id) {
        this.thread_id = thread_id;
    }

    public int getServer_id() {
        return server_id;
    }

    public void setServer_id(int server_id) {
        this.server_id = server_id;
    }

    public String getCommand_type() {
        return command_type;
    }

    public void setCommand_type(String command_type) {
        this.command_type = command_type;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }
}
