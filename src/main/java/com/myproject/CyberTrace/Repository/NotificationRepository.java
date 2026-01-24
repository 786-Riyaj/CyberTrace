package com.myproject.CyberTrace.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.CyberTrace.Model.Notification;
import com.myproject.CyberTrace.Model.Notification.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, Long>{

	List<Notification> findAllByStatus(NotificationStatus running);

	
}


