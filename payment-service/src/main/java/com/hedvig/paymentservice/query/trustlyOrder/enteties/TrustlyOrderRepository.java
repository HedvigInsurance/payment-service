package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrustlyOrderRepository extends CrudRepository<TrustlyOrder, UUID> {

  Optional<TrustlyOrder> findById(UUID id);

  TrustlyOrder findByTrustlyOrderId(String orderId);
}
