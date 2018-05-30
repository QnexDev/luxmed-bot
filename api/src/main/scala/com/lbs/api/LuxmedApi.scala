/**
 * MIT License
 *
 * Copyright (c) 2018 Yevhen Zadyra
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.lbs.api

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.lbs.api.http._
import com.lbs.api.http.headers._
import com.lbs.api.json.JsonSerializer.extensions._
import com.lbs.api.json.model._
import scalaj.http.{HttpRequest, HttpResponse}
import com.lbs.api.ApiResponseMutators._

object LuxmedApi extends ApiBase {

  private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  def login(username: String, password: String, clientId: String = "iPhone"): Either[Throwable, LoginResponse] = {
    val request = http("token").
      header(`Content-Type`, "application/x-www-form-urlencoded").
      header(`x-api-client-identifier`, clientId).
      param("client_id", clientId).
      param("grant_type", "password").
      param("password", password).
      param("username", username)
    post[LoginResponse](request)
  }

  def refreshToken(refreshToken: String, clientId: String = "iPhone"): Either[Throwable, LoginResponse] = {
    val request = http("token").
      header(`Content-Type`, "application/x-www-form-urlencoded").
      header(`x-api-client-identifier`, clientId).
      param("client_id", clientId).
      param("grant_type", "refresh_token").
      param("refresh_token", refreshToken)
    post[LoginResponse](request)
  }

  def reservedVisits(accessToken: String, tokenType: String, fromDate: ZonedDateTime = ZonedDateTime.now(),
                     toDate: ZonedDateTime = ZonedDateTime.now().plusMonths(3)): Either[Throwable, ReservedVisitsResponse] = {
    val request = http("visits/reserved").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken").
      param("fromDate", dateFormat.format(fromDate)).
      param("toDate", dateFormat.format(toDate))
    get[ReservedVisitsResponse](request).mutate
  }

  def visitsHistory(accessToken: String, tokenType: String, fromDate: ZonedDateTime = ZonedDateTime.now().minusYears(1),
                    toDate: ZonedDateTime = ZonedDateTime.now(), page: Int = 1, pageSize: Int = 100): Either[Throwable, VisitsHistoryResponse] = {
    val request = http("visits/history").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken").
      param("fromDate", dateFormat.format(fromDate)).
      param("toDate", dateFormat.format(toDate)).
      param("page", page.toString).
      param("pageSize", pageSize.toString)
    get[VisitsHistoryResponse](request).mutate
  }

  def reservationFilter(accessToken: String, tokenType: String, fromDate: ZonedDateTime = ZonedDateTime.now(),
                        toDate: Option[ZonedDateTime] = None, cityId: Option[Long] = None, clinicId: Option[Long] = None,
                        serviceId: Option[Long] = None): Either[Throwable, ReservationFilterResponse] = {
    val request = http("visits/available-terms/reservation-filter").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken").
      param("cityId", cityId.map(_.toString)).
      param("clinicId", clinicId.map(_.toString)).
      param("fromDate", dateFormat.format(fromDate)).
      param("toDate", toDate.map(dateFormat.format)).
      param("serviceId", serviceId.map(_.toString))
    get[ReservationFilterResponse](request).mutate
  }

  def availableTerms(accessToken: String, tokenType: String, payerId: Long, cityId: Long, clinicId: Option[Long], serviceId: Long, doctorId: Option[Long],
                     fromDate: ZonedDateTime = ZonedDateTime.now(), toDate: Option[ZonedDateTime] = None, timeOfDay: Int = 0,
                     languageId: Long = 10, findFirstFreeTerm: Boolean = false): Either[Throwable, AvailableTermsResponse] = {
    val request = http("visits/available-terms").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken").
      param("cityId", cityId.toString).
      param("doctorId", doctorId.map(_.toString)).
      param("findFirstFreeTerm", findFirstFreeTerm.toString).
      param("fromDate", dateFormat.format(fromDate)).
      param("languageId", languageId.toString).
      param("payerId", payerId.toString).
      param("clinicId", clinicId.map(_.toString)).
      param("serviceId", serviceId.toString).
      param("timeOfDay", timeOfDay.toString).
      param("toDate", dateFormat.format(toDate.getOrElse(fromDate.plusMonths(3))))
    get[AvailableTermsResponse](request).mutate
  }

  def temporaryReservation(accessToken: String, tokenType: String, temporaryReservationRequest: TemporaryReservationRequest): Either[Throwable, TemporaryReservationResponse] = {
    val request = http("visits/temporary-reservation").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken")
    post[TemporaryReservationResponse](request, bodyOpt = Some(temporaryReservationRequest))
  }

  def deleteTemporaryReservation(accessToken: String, tokenType: String, temporaryReservationId: Long): Either[Throwable, HttpResponse[String]] = {
    val request = http(s"visits/temporary-reservation/$temporaryReservationId").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken")
    delete(request)
  }

  def valuations(accessToken: String, tokenType: String, valuationsRequest: ValuationsRequest): Either[Throwable, ValuationsResponse] = {
    val request = http("visits/available-terms/valuations").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken")
    post[ValuationsResponse](request, bodyOpt = Some(valuationsRequest))
  }

  def reservation(accessToken: String, tokenType: String, reservationRequest: ReservationRequest): Either[Throwable, ReservationResponse] = {
    val request = http("visits/reserved").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken")
    post[ReservationResponse](request, bodyOpt = Some(reservationRequest))
  }

  def deleteReservation(accessToken: String, tokenType: String, reservationId: Long): Either[Throwable, HttpResponse[String]] = {
    val request = http(s"visits/reserved/$reservationId").
      header(`Content-Type`, "application/json").
      header(Authorization, s"$tokenType $accessToken")
    delete(request)
  }

  private def get[T <: SerializableJsonObject](request: HttpRequest)(implicit mf: scala.reflect.Manifest[T]): Either[Throwable, T] = {
    request.toEither.map(_.body.as[T])
  }

  private def post[T <: SerializableJsonObject](request: HttpRequest, bodyOpt: Option[SerializableJsonObject] = None)(implicit mf: scala.reflect.Manifest[T]): Either[Throwable, T] = {
    val postRequest = bodyOpt match {
      case Some(body) => request.postData(body.asJson)
      case None => request.postForm
    }
    postRequest.toEither.map(_.body.as[T])
  }

  private def delete(request: HttpRequest): Either[Throwable, HttpResponse[String]] = {
    request.postForm.method("DELETE").toEither
  }

}
