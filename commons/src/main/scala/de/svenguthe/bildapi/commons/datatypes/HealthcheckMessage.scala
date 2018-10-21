package de.svenguthe.bildapi.commons.datatypes

import de.svenguthe.bildapi.commons.datatypes.Actors.Actors
import de.svenguthe.bildapi.commons.datatypes.MessageStatus.MessageStatus
import org.joda.time.DateTime

case class HealthcheckMessage (module : Actors,
                               status : MessageStatus,
                               key : Any = None,
                               value : Any = None,
                               timestamp : DateTime = DateTime.now())