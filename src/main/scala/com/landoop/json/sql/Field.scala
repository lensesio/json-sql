/*
 * Copyright 2017 Landoop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.landoop.json.sql

import org.apache.calcite.sql._

import scala.collection.JavaConversions._

/**
  * Created by stefan on 17/04/2017.
  */
case class Field(name: String, alias: String, parents: Vector[String]) {
  def hasParents: Boolean = parents != null && parents.nonEmpty
}

object Field {
  def from(sql: SqlSelect): Seq[Field] = {
    sql.getSelectList.map {
      case id: SqlIdentifier =>
        val parents = (0 until id.names.length - 1).foldLeft(Vector.empty[String]) { (acc, i) =>
          val parent = id.names.get(i)
          acc :+ parent
        }
        if (id.isStar) {
          Field("*", "*", if(parents.isEmpty) null else parents)
        } else {
          Field(id.names.last, id.names.last, if(parents.isEmpty) null else parents)
        }
      case as: SqlCall if as.getKind == SqlKind.AS && as.operandCount() == 2 =>
        val left: SqlIdentifier = as.operand[SqlNode](0) match {
          case id: SqlIdentifier => id
          case other => throw new IllegalArgumentException(s"$as [${as.getClass.getCanonicalName}] is not handled for now!")
        }

        val right: SqlIdentifier = as.operand[SqlNode](1) match {
          case id: SqlIdentifier => id
          case other => throw new IllegalArgumentException(s"$as [${as.getClass.getCanonicalName}] is not handled for now!")
        }

        val parents = (0 until left.names.length - 1).foldLeft(Vector.empty[String]) { (acc, i) =>
          val parent = left.names.get(i)
          acc :+ parent
        }
        Field(left.names.last, right.names.last, if(parents.isEmpty) null else parents)

      case other => throw new IllegalArgumentException(s"$other [${other.getClass.getCanonicalName}] is not handled for now!")
    }.toSeq
  }
}