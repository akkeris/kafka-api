package models.http

import models.AclRole.AclRole
import models.Models.{ AclCredentials, Topic }
import play.api.libs.json._

trait HttpRequest
sealed trait HttpResponse

object HttpModels {
  final case class Error(title: String, detail: String)
  final case class ErrorResponse(errors: Seq[Error])

  final case class TopicRequest(topic: Topic) extends HttpRequest
  final case class TopicResponse(topic: Topic) extends HttpResponse
  final case class AclRequest(topic: String, user: String, role: AclRole) extends HttpRequest
  final case class AclResponse(aclCredentials: AclCredentials) extends HttpResponse

  implicit val topicRequestFormat = Json.format[TopicRequest]
  implicit val topicResponseFormat = Json.format[TopicResponse]
  implicit val aclRequestFormat = Json.format[AclRequest]
  implicit val aclResponseFormat = Json.format[AclResponse]
  implicit val errorFormat = Json.format[Error]
  implicit val errorRespFormat = Json.format[ErrorResponse]

  def createErrorResponse(title: String, message: String): ErrorResponse = ErrorResponse(Seq(Error(title, message)))
}