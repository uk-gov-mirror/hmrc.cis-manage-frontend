/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

sealed trait Target
object Target {
  case object Returns extends Target
  case object Notices extends Target
  case object Subcontractor extends Target
  case object ManageContractorDetails extends Target

  private val returnsKeys = Set(
    "returnDue",
    "returnHistory",
    "amendReturn",
    "manageYourCisReturn"
  )

  private val noticesKeys = Set(
    "newNotices",
    "noticesAndStatements"
  )

  private val subcontractorKeys = Set(
    "subcontractors"
  )

  private val manageContractorDetailsKeys = Set(
    "contractorDetails"
  )

  def fromKey(key: String): Option[Target] = key match {
    case k if returnsKeys.contains(k)                 => Some(Returns)
    case k if noticesKeys.contains(k)                 => Some(Notices)
    case k if subcontractorKeys.contains(k)           => Some(Subcontractor)
    case k if manageContractorDetailsKeys.contains(k) => Some(ManageContractorDetails)
    case _                                            => None
  }
}
