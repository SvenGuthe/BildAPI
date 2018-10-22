package de.svenguthe.bildapi.commons.datatypes

import de.svenguthe.bildapi.commons.datatypes.ActivityActorMessages.ActivityActorMessages
import de.svenguthe.bildapi.commons.datatypes.Actors.Actors
import de.svenguthe.bildapi.commons.datatypes.MessageStatus.MessageStatus
import org.joda.time.DateTime

case class HealthcheckMessage (module : Actors,
                               classname : String,
                               status : MessageStatus,
                               action : ActivityActorMessages,
                               value : Any = None,
                               timestamp : DateTime = DateTime.now())