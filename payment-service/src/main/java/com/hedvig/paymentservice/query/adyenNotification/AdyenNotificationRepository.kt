package com.hedvig.paymentservice.query.adyenNotification

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AdyenNotificationRepository : CrudRepository<AdyenNotification, Long>
