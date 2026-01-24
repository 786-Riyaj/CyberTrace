package com.myproject.CyberTrace.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Model.Users;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {



	List<Complaint> findAllByStatus(ComplaintStatus pending);

	List<Complaint> findAllByStatusOrStatus(ComplaintStatus proccessing, ComplaintStatus resolved);

	Complaint findByComplaintId(String cid);

	boolean existsByComplaintId(String cid);

	List<Complaint> findAllByStatusAndAssignedTo(ComplaintStatus proccessing, Users investigator);

	long countByAssignedTo(Users investigator);
	
	long countByAssignedToAndStatus(
            Users investigator,
            ComplaintStatus status);
	
	List<Complaint> findByAssignedTo(Users investigator);
	// ✅ Monthly Complaint Counts (Native SQL)
    @Query(value = "SELECT MONTH(c.reg_date_time) AS month, COUNT(*) AS total " +
                   "FROM complaint c " +
                   "GROUP BY MONTH(c.reg_date_time) " +
                   "ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyComplaintStats();
	
}
