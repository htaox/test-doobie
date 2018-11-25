package com.eztier.postgres.eventstore.models

import doobie._
import io.circe.{Json, Encoder, Decoder, HCursor}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto.{ deriveDecoder, deriveEncoder }

/*
update hl7.patient
set current = ('{"id": "'|| name ||'", "name": "' || name || '", 
"nameComponents": [
  {
    "academic": "",
    "firstName": "' || name || '",
    "givenName": "",
    "initials": "", 
    "lastName": "' || name || '",
    "lastNameFromSpouse": "",
    "lastNamePrefix": "",
    "middleName": "",
    "preferredName": "",
    "preferredNameType": "",
    "spouseLastNameFirst": "",
    "spouseLastNamePrefix": "",
    "suffix": "",
    "title": ""    
  }
  ]
}')::jsonb


*/

case class CaPatientNameComponents(
  Academic: String = "",
  FirstName: String = "",
  GivenName: String = "",
  Initials: String = "",
  LastName: String = "",
  LastNameFromSpouse: String = "",
  LastNamePrefix: String = "",
  MiddleName: String = "",
  PreferredName: String = "",
  PreferredNameType: String = "",
  SpouseLastNameFirst: String = "",
  SpouseLastNamePrefix: String = "",
  Suffix: String = "",
  Title: String = ""
)

case class CaPatient(
  Id: String, name: String,
  NameComponents: Seq[CaPatientNameComponents] = Seq()
) extends Model

private object CaPatient{
  import doobie.postgres.circe.jsonb.implicits._
  
  val renameKeys = (name: String) => name.charAt(0).toLower.toString + name.substring(1)

  // implicit val customConfiguration: Configuration = Configuration(renameKeys, Predef.identity, false, None)
  implicit val customConfig: Configuration = Configuration.default.copy(transformMemberNames = renameKeys)

  implicit val caPatientEncoder: Encoder[CaPatient] = deriveEncoder
  implicit val caPatientDecoder: Decoder[CaPatient] = deriveDecoder
  /*
  implicit val caPatientEncoder: Encoder[CaPatient] = new Encoder[CaPatient] {
    final def apply(a: CaPatient): Json = Json.obj(
      ("id", Json.fromString(a.Id)),
      ("name", Json.fromString(a.Name))
    )
  }
  
  implicit val caPatientDecoder: Decoder[CaPatient] = new Decoder[CaPatient] {
    final def apply(c: HCursor): Decoder.Result[CaPatient] =
    for {
      id <- c.downField("id").as[String]
      name <- c.downField("name").as[String]
      nameComponents <- c.downField("nameComponents").as[Seq[CaPatientNameComponents]]
    } yield {
      new CaPatient(Id = id, Name = name, NameComponents = nameComponents)
    }
  }
  */

  implicit val caPatientGet : Get[CaPatient] = pgDecoderGetT[CaPatient]
  implicit val caPatientPut : Put[CaPatient] = pgEncoderPutT[CaPatient]
}
