package app.PayRouteX.repository;

import app.PayRouteX.entity.Tasks;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TasksRepository {

    public void saveTask(Tasks tasks) {

    }

    public Tasks findById(Integer id) {
      return null;
    }

    public List<Tasks> findAll() {
        return null;
    }

    public boolean existsById(Integer id) {
        return false;
    }

    public void deleteById(Integer id) {

    }
}
