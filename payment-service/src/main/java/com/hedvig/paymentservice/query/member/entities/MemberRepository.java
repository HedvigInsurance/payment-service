package com.hedvig.paymentservice.query.member.entities;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends CrudRepository<Member, String> {
    Optional<Member> findById(String id);
    Optional<Boolean> findByIdAndByDirectDebitMandateActiveTrue(String id);
    List<Member> findByDirectDebitMandateActive(Boolean isActive);
}
