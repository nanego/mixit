package mixit.controller

import mixit.model.Ticket
import mixit.repository.TicketRepository
import mixit.util.RouterFunctionProvider
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.Routes
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import java.util.*

@Controller
class TicketingController(val repository: TicketRepository) : RouterFunctionProvider() {

    override val routes: Routes = {
        accept(MediaType.TEXT_HTML).route {
            GET("/ticketing") { ok().render("ticketing") }
        }
        contentType(MediaType.APPLICATION_FORM_URLENCODED).route {
            POST("/ticketing", this@TicketingController::submit)
        }
    }

    fun submit(req: ServerRequest) = req.body(BodyExtractors.toFormData()).then { data ->
        val formData  = data.toSingleValueMap()
        val ticket = Ticket(formData["email"]!!,
                formData["firstname"]!!,
                formData["lastname"]!!)
        repository.save(ticket)
                .then { t -> ok().render("ticketing-submission", formData) }
                .otherwise(DuplicateKeyException::class.java, { ok().render("ticketing-error", mapOf(Pair("message", "ticketing.error.alreadyexists"))) } )
                .otherwise { ok().render("ticketing-error", mapOf(Pair("message", "ticketing.error.default"))) }
    }
}
