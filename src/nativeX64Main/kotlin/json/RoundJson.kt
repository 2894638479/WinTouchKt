package json

import button.Round
import kotlinx.serialization.Serializable

@Serializable
class RoundJson(
    val x:Float,
    val y:Float,
    val r:Float
){
    fun toRound() = Round(x, y, r)
}