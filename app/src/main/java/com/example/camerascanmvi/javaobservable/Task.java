package com.example.camerascanmvi.javaobservable;

public class Task {

    String taskname;
    boolean iscomplete;
    int prirotry;

    public Task(String taskname, boolean iscomplete, int prirotry) {
        this.taskname = taskname;
        this.iscomplete = iscomplete;
        this.prirotry = prirotry;
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }

    public boolean isIscomplete() {
        return iscomplete;
    }

    public void setIscomplete(boolean iscomplete) {
        this.iscomplete = iscomplete;
    }

    public int getPrirotry() {
        return prirotry;
    }

    public void setPrirotry(int prirotry) {
        this.prirotry = prirotry;
    }
}
