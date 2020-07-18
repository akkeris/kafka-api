package controllers

import javax.inject.Inject
import models.http.HttpModels.{ MigrationStage1Request, MigrationStage2Request, TopicRequest, TopicResponse, createErrorResponse }
import play.api.Logger
import play.api.libs.json.{ JsError, JsPath, Json, JsonValidationError }
import play.api.mvc.{ InjectedController, Result }
import services.MigrationService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MigrationController @Inject() (service: MigrationService) extends InjectedController with RequestProcessor {

  val logger = Logger(this.getClass)

  def migrateStage1() = Action(parse.json) { implicit request =>
    Future {
      service.migrateStage1(request.body.as[MigrationStage1Request])
    }
    Accepted
  }

  //  def migrateStage1() = Action.async { implicit request =>
  //    val req = request.body.asJson.get.as[MigrationStage1Request]
  //    Future {
  //      service.migrateStage1(req)
  //    }.map(_ => Ok)
  //  }

  def migrateStage2() = Action.async { implicit request =>
    processRequest[MigrationStage2Request](migrateStage2Request())
    Future.successful(Accepted)
  }

  private def migrateStage2Request()(request: MigrationStage2Request): Future[Result] = {
    Future {
      service.migrateStage2(request)
      Ok
    }
  }
}
