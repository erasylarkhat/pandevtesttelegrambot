package io.proj3ct.SpringDemoBot.repository;

import io.proj3ct.SpringDemoBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByChatId(Long chatId);
}
