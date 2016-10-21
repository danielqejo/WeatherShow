package br.com.danielsantana.weathershow.task.callback;

public interface TaskCallback {

    void onTaskSucess(Integer taskId, Object object);
    void onTaskFailure(Exception exception);
}
