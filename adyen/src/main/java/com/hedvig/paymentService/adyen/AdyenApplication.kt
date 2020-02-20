package com.hedvig.paymentService.adyen

import com.adyen.Client
import com.adyen.constants.ApiConstants.PaymentMethodType
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethodsRequest
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.service.Checkout
import com.google.gson.Gson
import java.util.UUID


class AdyenApplication() {
}

fun getAvailablePaymentMethods(): Any {
  val client = Client(
    "AQEjhmfuXNWTK0Qc+iSYl2AuruO6asUZ5ply6PqxJFV7zNpfI3YQwV1bDb7kfNy1WIxIIkxgBw==-t/rRF15rJzW/bib2yDV5vGIL9AdzPvSMeRXcvojGiCA=-#t.:+R5>++VJg5%V",
    Environment.TEST
  )

  val checkout = Checkout(client)

  val paymentMethodsRequest = PaymentMethodsRequest()

  paymentMethodsRequest.merchantAccount = "HedvigABCOM"

  paymentMethodsRequest.countryCode = "SE"

  val amount = Amount()

  amount.setCurrency("SEK")

  amount.setValue(99L)

  paymentMethodsRequest.amount = amount

  paymentMethodsRequest.channel = PaymentMethodsRequest.ChannelEnum.WEB

  val response = checkout.paymentMethods(paymentMethodsRequest)

  return response
}

fun makePayment(): Any {
  val client = Client(
    "AQEjhmfuXNWTK0Qc+iSYl2AuruO6asUZ5ply6PqxJFV7zNpfI3YQwV1bDb7kfNy1WIxIIkxgBw==-t/rRF15rJzW/bib2yDV5vGIL9AdzPvSMeRXcvojGiCA=-#t.:+R5>++VJg5%V",
    Environment.TEST
  )

  val checkout = Checkout(client)

  val paymentsRequest = PaymentsRequest()

  paymentsRequest.merchantAccount = "HedvigABCOM"

  val amount = Amount()

  amount.currency = "SEK"

  amount.value = 1500L

  paymentsRequest.amount = amount

  paymentsRequest.reference = UUID.randomUUID().toString()

  paymentsRequest.shopperReference("12345")

  paymentsRequest.recurringProcessingModel = PaymentsRequest.RecurringProcessingModelEnum.SUBSCRIPTION
  paymentsRequest.storePaymentMethod(true)

  paymentsRequest.addEncryptedCardData(
    "adyenjs_0_1_25" +
      "\$ZHYLUAP1QoWuXodRyDF/MHt/jI+EJv+Gu3jbGcLfrXpmQW9ljcn6pLfbXGVghWMpEn2eZjKkhjeJBwel1hnrhAhxKUzvE" +
      "+tV7OgvnOWgolPi1C2MW5WZBaDqZWKO9CjyBZLXE/0xen+h1nnBzH+ZEJ4b7IJr2v27/jSnrUZzFWrhCoNs5FsTTkWEkSuqi" +
      "3yPEW0leJ+GS3GYoi/++VPurcbDIdXOdaVR7PhT9DMmcLh7m1mLNfZFxx9XcTha2HOdL0J0cwiIa50uLNSNbj/W1qliMVeORYWnHUAGoH7yF5T8J9Y3Qs89Id" +
      "9budRlvxFJ/egQKRWCSoWEy9aegHfewA==\$fSUuzYGr/drT28uK459qjVUzEI0OOQcnmQM8flpznV4g4v5ERie4sKk3uScDgNGxh1u1cy4C3dPc5HkoIb0yK" +
      "cKnZcTK9XxLcpNiiSzYjsJdOY+qV5tczcgC5Hz6nMZ1nyzJ4Akj4sdO2bMKNOVe3jrxJs+tjc2A/JjOSs196wsDxcXqaS840nR6n1J53k7lSM0D2AbqkXCbIz" +
      "KTHyTcxJOlvYkfo+lw4TGPc8RDilezkyiWiBqpO8JtZNpuB928BgqMhHFCEtgnwHDZX9UqilrQHiRPt7uUh5HBI81u/KepwgAaZxXXzW3ue2EXnmUGCgUFEOx" +
      "NNZBgY3Crf+jOP16H+uMm/KOnM6M8JGyCbCUTQKMAPAwWvDnIkjvlhCYKKwQUm3b5E1qq47SDLCdXq9Y+X0mc+ZtAG2KAL5EASeZQBpVjTILb+SkqOk3VfRj4" +
      "LxplBl+RawNLgvzlFrWj+27jxi5Txd6ySwN/AYt5xU1lw9EcV3sOLwv5bMX0n2sYFBHwwaLzOl4eGYG00dYcNWcc7YQ2BekdmWmQf/re/EV3U+lROgS8QLVfa" +
      "KI3mhbYl+q5nJK9nMGADtBPtpcXMKa+yL0u4n5eqxAU++AqbqCNyTx9K6BZphQ4CRv5mr5aCTub/h0AcgQ4QBlIHekV10//o0NqK65mwPVebt3pEQ5BhtcF9r" +
      "nU3OABapwLDEEUDQruQ76TroyIQ5VaHvkCBjcXvVLxDzFqQdPu61uDdmomGww0PHAEXfRII9pHnQiwKztdEkSQkSfsoLSwWeV7kojiq143bmuppyJ7",
    "adyenjs_0_1_25\$ggWuuN/DSSIUEat+z4kqvSmuVG1si0y2C3MdYtLQSfFiQ34aFOh5wXEmSIzFAle9TIznQ2HM4Xb4CYg2" +
      "XGJasP1Akk239IZHsz92I4TP73Zp5WK8hfki8no6aElyQpPuSfFq3elsdHtp5Y91C/5V8Yz/CGIFbpbgaQrWJZoME8XpXqYCacBDrI7o9FCx/UowTWEIOd9sf" +
      "rzS9sijXShsmwwm73jVdphvRoS5eNMyXiU3VDYYWzQrTlOU5GCspsnELo7cH2bZlGsbZ2hpnFFMu8mZRCsY1aXSqCUdHGvih7/kOokVJRFlg0wqvcxrWiNuqN" +
      "2DYCraUJCSRSlAVOD67g==\$QlwiKU0FK1P/mh8PAgTNzX112q4xUDq5JAfHL3qGeJBtfmZD94W659FW6V7GGA/vAV8gq8g4TPlYY6BDkNhE1iyGhngu98La0" +
      "OwH/9p0NonohEsUcqpFUxjcR+Wne53EINYm0e+169mAO735/tIBxUGY3gtEoBD6xfmx76Jip/bY5fUsGZ74lR2f/SbK7TGJr8mMpryLVdaOgV7NaeyQjVCI4X" +
      "gTiLB2K57mOLv+YgLatWkt0oMqHe+5NX3zP9sOO6lnJea22MKls13nxRZ36F6aAm2nCtZsw9YqPwFmnlajs5GYbm2m9MhAHShmkQKPqSTGdhToUiEtbr0l3wU" +
      "mB78HOIS/SfFBW439T4eTY1dWHVRLA8qInfZulLJMoMGyCMeSMI2EaPXueMYYQ7P7YxRfzCqwxRS2yXRhfMHgTP8CRuK51p3+b7bXPuyou/Eywybtzm3t3XmW" +
      "eAgo5+cNN4zXiaXjbM/N",
    "adyenjs_0_1_25\$PM7ThIEGP1PR/tC3lhSQrStGGnRFGHv6ob/Vh6BOD3fyXnPWm99wOlci2m0ZMopjdjGSOsnXuDln4zGpRVc" +
      "GfwzrqMaCzbzq2ZRGwZ44i6s8GSPUncs4bxVeUYpTk3fpGmG2juiehyyeKzOmNb5KcD6S8256iCLohDfOGUF9rUF8O7GpPTAXyVxQ5rWKQb9RjkpFrhbGdsv0xV" +
      "eoRTO6JdDZi+BzEUDLMJgEQEh7Hwx3QbIZy0rttNKf/4YrS/R/mjFXHtdQUJp/nNlqqm5KMze36ZFIIQe39TUjsar0tAuSRRF//1Y/av/BnWLKHNNDlCJ2HRyn0" +
      "HpOGCcloik5ZA==\$A3NSbC04yiSsBe8kP+15qNvPRLBrNXWlx8HHnefX1834Xm1CkKRtb4ot0DnKeunFKXfU4QlgcuoTOhqKYcTVxAwLjCEqiALiu7E0s35dba" +
      "sVeIzntR4Y+OZgpC0Cw/ncGWPaE4voZL4MRjS5hFXD+xrftzER/WORgmHpV+i0Uf0SeRGHY142mO5grH+j2XWZz6Pv+ZLiWZWFsBgwnfY1ksB7b7Y4tqg5NiQA8" +
      "QHwUJZ1cO0+vWQ7Ev7IxYXD+ebLeOwm7/p9UNReH61ZW4rRY3cbxt9JlG20Rs6eBohYKygk3vrfoXRQNfB+Kyll7TA1J+5TKV9nnsd2gRIB9OSuNfFuiEQs8N8q" +
      "YlIYpjw7zPYWILXqJjxrAQwETP0pp9CjpPwdp50Dh6G9JAUe6NfJM8enGeTo7EYlK9lJ0KWO3q5jVT4sd/Xf92T68aILYpydN1Y4hKhVOHoPQvurmc4Sv+tQTXz" +
      "cRmBMVQ==",
    "adyenjs_0_1_25\$sFyntF60Y2wcs0/ysla79MJ9y7+owYwupP39FZj5vXJS+WP6A8qrLjlE7E3MuXbT48xDiWsQPCqW3hV9qprn" +
      "A0HURpRW6kngVjLqRq8Hb4pE/rjX4Lh6h6TPSNcL4iJkzTEESA+FXKVF5TjWTI9OKrymbA2NZxiCh7RNNJGJkh4ZykhjyMSwDsDHK4k5zc1+AC37SpTXeDuJXN9Cmv" +
      "V4Mi4Ve8BcOSSNWZHnkeDYNWEqM3zX1JkmVNbszj5+FEB3BBL7pZqHOr8r0gPkof9dsCstt92Ogs+aaSFAH3fgPNl/9AlCoklr1Q6ARsplkbGLlcDKpPRgCZVChT5N" +
      "V0q3Sw==\$TkkK2N55VUxkNo5L4NlDgCnyADdjn58nCHQJ69MjYfz8ufJ9IQ1K7i3hZqz4Q7V6vPb+Giys8yYo7wyzm+PWrelmDTzWcNyfqahRbQNXhY90du90qVDG" +
      "0DviK4JYThPNnlQOZtIDvIyNQQSI8oJEDydN7sOL8ZvfIV1FxhkIMrb6tkzJJi+WYWMEFDdAcacf0sswSFHUSTNVWuz6WCjrP22qMt1krUHYjbJp0+uS91TJcYN8vT" +
      "Y4gcMHPCO52F8d5HPufLfz7YKYQbXYtkXFz57c4K3dXVJDQ9AXwECNjlRRpuMZzINuEQhkxsBMKNRJBSCW2OX+qScUSIg+vZxcFyWJnOoLt0oKFL3zFj2vjsLsdwFd" +
      "n7tkDt5NwPDDWek8ZbOwrBWBzCVP408mxB1kjQFs2867DxnxGCQr3bpyGsgOgvVJnw+vWfIlE6priv7CwsTC1WFk1yNTYYibSQK05S7q",
    "John Smith"
  )

  paymentsRequest.returnUrl = "http://localhost"

  val paymentsResponse = checkout.payments(paymentsRequest)

  return paymentsResponse
}


fun storeToken(): PaymentsResponse {
  val client = Client(
    "AQEjhmfuXNWTK0Qc+iSYl2AuruO6asUZ5ply6PqxJFV7zNpfI3YQwV1bDb7kfNy1WIxIIkxgBw==-t/rRF15rJzW/bib2yDV5vGIL9AdzPvSMeRXcvojGiCA=-#t.:+R5>++VJg5%V",
    Environment.TEST
  )

  val checkout = Checkout(client)

  val paymentsRequest = PaymentsRequest()

  paymentsRequest.merchantAccount = "HedvigABCOM"

  val amount = Amount()

  amount.currency = "SEK"

  amount.value = 0L

  paymentsRequest.amount = amount

  paymentsRequest.reference = UUID.randomUUID().toString()

  paymentsRequest.shopperReference = "Hedvig-12345"
  paymentsRequest.shopperInteraction = PaymentsRequest.ShopperInteractionEnum.ECOMMERCE
  paymentsRequest.recurringProcessingModel = PaymentsRequest.RecurringProcessingModelEnum.SUBSCRIPTION
  paymentsRequest.storePaymentMethod = true

  paymentsRequest.addEncryptedCardData(
    "adyenjs_0_1_25" +
      "\$ZHYLUAP1QoWuXodRyDF/MHt/jI+EJv+Gu3jbGcLfrXpmQW9ljcn6pLfbXGVghWMpEn2eZjKkhjeJBwel1hnrhAhxKUzvE" +
      "+tV7OgvnOWgolPi1C2MW5WZBaDqZWKO9CjyBZLXE/0xen+h1nnBzH+ZEJ4b7IJr2v27/jSnrUZzFWrhCoNs5FsTTkWEkSuqi" +
      "3yPEW0leJ+GS3GYoi/++VPurcbDIdXOdaVR7PhT9DMmcLh7m1mLNfZFxx9XcTha2HOdL0J0cwiIa50uLNSNbj/W1qliMVeORYWnHUAGoH7yF5T8J9Y3Qs89Id" +
      "9budRlvxFJ/egQKRWCSoWEy9aegHfewA==\$fSUuzYGr/drT28uK459qjVUzEI0OOQcnmQM8flpznV4g4v5ERie4sKk3uScDgNGxh1u1cy4C3dPc5HkoIb0yK" +
      "cKnZcTK9XxLcpNiiSzYjsJdOY+qV5tczcgC5Hz6nMZ1nyzJ4Akj4sdO2bMKNOVe3jrxJs+tjc2A/JjOSs196wsDxcXqaS840nR6n1J53k7lSM0D2AbqkXCbIz" +
      "KTHyTcxJOlvYkfo+lw4TGPc8RDilezkyiWiBqpO8JtZNpuB928BgqMhHFCEtgnwHDZX9UqilrQHiRPt7uUh5HBI81u/KepwgAaZxXXzW3ue2EXnmUGCgUFEOx" +
      "NNZBgY3Crf+jOP16H+uMm/KOnM6M8JGyCbCUTQKMAPAwWvDnIkjvlhCYKKwQUm3b5E1qq47SDLCdXq9Y+X0mc+ZtAG2KAL5EASeZQBpVjTILb+SkqOk3VfRj4" +
      "LxplBl+RawNLgvzlFrWj+27jxi5Txd6ySwN/AYt5xU1lw9EcV3sOLwv5bMX0n2sYFBHwwaLzOl4eGYG00dYcNWcc7YQ2BekdmWmQf/re/EV3U+lROgS8QLVfa" +
      "KI3mhbYl+q5nJK9nMGADtBPtpcXMKa+yL0u4n5eqxAU++AqbqCNyTx9K6BZphQ4CRv5mr5aCTub/h0AcgQ4QBlIHekV10//o0NqK65mwPVebt3pEQ5BhtcF9r" +
      "nU3OABapwLDEEUDQruQ76TroyIQ5VaHvkCBjcXvVLxDzFqQdPu61uDdmomGww0PHAEXfRII9pHnQiwKztdEkSQkSfsoLSwWeV7kojiq143bmuppyJ7",
    "adyenjs_0_1_25\$ggWuuN/DSSIUEat+z4kqvSmuVG1si0y2C3MdYtLQSfFiQ34aFOh5wXEmSIzFAle9TIznQ2HM4Xb4CYg2" +
      "XGJasP1Akk239IZHsz92I4TP73Zp5WK8hfki8no6aElyQpPuSfFq3elsdHtp5Y91C/5V8Yz/CGIFbpbgaQrWJZoME8XpXqYCacBDrI7o9FCx/UowTWEIOd9sf" +
      "rzS9sijXShsmwwm73jVdphvRoS5eNMyXiU3VDYYWzQrTlOU5GCspsnELo7cH2bZlGsbZ2hpnFFMu8mZRCsY1aXSqCUdHGvih7/kOokVJRFlg0wqvcxrWiNuqN" +
      "2DYCraUJCSRSlAVOD67g==\$QlwiKU0FK1P/mh8PAgTNzX112q4xUDq5JAfHL3qGeJBtfmZD94W659FW6V7GGA/vAV8gq8g4TPlYY6BDkNhE1iyGhngu98La0" +
      "OwH/9p0NonohEsUcqpFUxjcR+Wne53EINYm0e+169mAO735/tIBxUGY3gtEoBD6xfmx76Jip/bY5fUsGZ74lR2f/SbK7TGJr8mMpryLVdaOgV7NaeyQjVCI4X" +
      "gTiLB2K57mOLv+YgLatWkt0oMqHe+5NX3zP9sOO6lnJea22MKls13nxRZ36F6aAm2nCtZsw9YqPwFmnlajs5GYbm2m9MhAHShmkQKPqSTGdhToUiEtbr0l3wU" +
      "mB78HOIS/SfFBW439T4eTY1dWHVRLA8qInfZulLJMoMGyCMeSMI2EaPXueMYYQ7P7YxRfzCqwxRS2yXRhfMHgTP8CRuK51p3+b7bXPuyou/Eywybtzm3t3XmW" +
      "eAgo5+cNN4zXiaXjbM/N",
    "adyenjs_0_1_25\$PM7ThIEGP1PR/tC3lhSQrStGGnRFGHv6ob/Vh6BOD3fyXnPWm99wOlci2m0ZMopjdjGSOsnXuDln4zGpRVc" +
      "GfwzrqMaCzbzq2ZRGwZ44i6s8GSPUncs4bxVeUYpTk3fpGmG2juiehyyeKzOmNb5KcD6S8256iCLohDfOGUF9rUF8O7GpPTAXyVxQ5rWKQb9RjkpFrhbGdsv0xV" +
      "eoRTO6JdDZi+BzEUDLMJgEQEh7Hwx3QbIZy0rttNKf/4YrS/R/mjFXHtdQUJp/nNlqqm5KMze36ZFIIQe39TUjsar0tAuSRRF//1Y/av/BnWLKHNNDlCJ2HRyn0" +
      "HpOGCcloik5ZA==\$A3NSbC04yiSsBe8kP+15qNvPRLBrNXWlx8HHnefX1834Xm1CkKRtb4ot0DnKeunFKXfU4QlgcuoTOhqKYcTVxAwLjCEqiALiu7E0s35dba" +
      "sVeIzntR4Y+OZgpC0Cw/ncGWPaE4voZL4MRjS5hFXD+xrftzER/WORgmHpV+i0Uf0SeRGHY142mO5grH+j2XWZz6Pv+ZLiWZWFsBgwnfY1ksB7b7Y4tqg5NiQA8" +
      "QHwUJZ1cO0+vWQ7Ev7IxYXD+ebLeOwm7/p9UNReH61ZW4rRY3cbxt9JlG20Rs6eBohYKygk3vrfoXRQNfB+Kyll7TA1J+5TKV9nnsd2gRIB9OSuNfFuiEQs8N8q" +
      "YlIYpjw7zPYWILXqJjxrAQwETP0pp9CjpPwdp50Dh6G9JAUe6NfJM8enGeTo7EYlK9lJ0KWO3q5jVT4sd/Xf92T68aILYpydN1Y4hKhVOHoPQvurmc4Sv+tQTXz" +
      "cRmBMVQ==",
    "adyenjs_0_1_25\$sFyntF60Y2wcs0/ysla79MJ9y7+owYwupP39FZj5vXJS+WP6A8qrLjlE7E3MuXbT48xDiWsQPCqW3hV9qprn" +
      "A0HURpRW6kngVjLqRq8Hb4pE/rjX4Lh6h6TPSNcL4iJkzTEESA+FXKVF5TjWTI9OKrymbA2NZxiCh7RNNJGJkh4ZykhjyMSwDsDHK4k5zc1+AC37SpTXeDuJXN9Cmv" +
      "V4Mi4Ve8BcOSSNWZHnkeDYNWEqM3zX1JkmVNbszj5+FEB3BBL7pZqHOr8r0gPkof9dsCstt92Ogs+aaSFAH3fgPNl/9AlCoklr1Q6ARsplkbGLlcDKpPRgCZVChT5N" +
      "V0q3Sw==\$TkkK2N55VUxkNo5L4NlDgCnyADdjn58nCHQJ69MjYfz8ufJ9IQ1K7i3hZqz4Q7V6vPb+Giys8yYo7wyzm+PWrelmDTzWcNyfqahRbQNXhY90du90qVDG" +
      "0DviK4JYThPNnlQOZtIDvIyNQQSI8oJEDydN7sOL8ZvfIV1FxhkIMrb6tkzJJi+WYWMEFDdAcacf0sswSFHUSTNVWuz6WCjrP22qMt1krUHYjbJp0+uS91TJcYN8vT" +
      "Y4gcMHPCO52F8d5HPufLfz7YKYQbXYtkXFz57c4K3dXVJDQ9AXwECNjlRRpuMZzINuEQhkxsBMKNRJBSCW2OX+qScUSIg+vZxcFyWJnOoLt0oKFL3zFj2vjsLsdwFd" +
      "n7tkDt5NwPDDWek8ZbOwrBWBzCVP408mxB1kjQFs2867DxnxGCQr3bpyGsgOgvVJnw+vWfIlE6priv7CwsTC1WFk1yNTYYibSQK05S7q",
    "John Smith"
  )

  paymentsRequest.returnUrl = "http://www.hedvig.com"

  val paymentsResponse = checkout.payments(paymentsRequest)

  return paymentsResponse
}


fun makePaymentWithToken(): Any {
  val client = Client(
    "AQEjhmfuXNWTK0Qc+iSYl2AuruO6asUZ5ply6PqxJFV7zNpfI3YQwV1bDb7kfNy1WIxIIkxgBw==-t/rRF15rJzW/bib2yDV5vGIL9AdzPvSMeRXcvojGiCA=-#t.:+R5>++VJg5%V",
    Environment.TEST
  )

  val checkout = Checkout(client)

  val paymentsRequest = PaymentsRequest()

  paymentsRequest.merchantAccount = "HedvigABCOM"

  val amount = Amount()

  amount.currency = "SEK"

  amount.value = 15000L

  paymentsRequest.amount = amount

  paymentsRequest.reference = UUID.randomUUID().toString()

  paymentsRequest.shopperReference = "Hedvig-12345"
  paymentsRequest.shopperInteraction = PaymentsRequest.ShopperInteractionEnum.CONTAUTH
  paymentsRequest.recurringProcessingModel = PaymentsRequest.RecurringProcessingModelEnum.SUBSCRIPTION


  val paymentMethodDetails = DefaultPaymentMethodDetails()

  paymentMethodDetails
    .type(PaymentMethodType.TYPE_SCHEME)
    .recurringDetailReference("8315821268475772")

  paymentsRequest.paymentMethod = paymentMethodDetails

  paymentsRequest.returnUrl = "http://www.hedvig.com"

  val paymentsResponse = checkout.payments(paymentsRequest)

  return paymentsResponse
}


fun getCardDetails(): Any {
  val client = Client(
    "AQEjhmfuXNWTK0Qc+iSYl2AuruO6asUZ5ply6PqxJFV7zNpfI3YQwV1bDb7kfNy1WIxIIkxgBw==-t/rRF15rJzW/bib2yDV5vGIL9AdzPvSMeRXcvojGiCA=-#t.:+R5>++VJg5%V",
    Environment.TEST
  )

  val checkout = Checkout(client)

  return checkout.paymentMethods(
    PaymentMethodsRequest()
      .merchantAccount("HedvigABCOM")
      .shopperReference("Hedvig-12345")
  )
}

fun main() {
//  val methods = getAvailablePaymentMethods()
//  println(Gson().toJson(methods))

//  val payment = makePayment()
//  println(Gson().toJson(payment))

//  val tokenize = storeToken()
//  println(Gson().toJson(tokenize))

//  val paymentWithToken = makePaymentWithToken()
//  println(Gson().toJson(paymentWithToken))

  val cardDetails = getCardDetails()
  println(Gson().toJson(cardDetails))
}

