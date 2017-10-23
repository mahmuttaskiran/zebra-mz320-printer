package opcon.com.printer

import java.util.*

/**
 * Created by aslitaskiran on 15/10/2017.
 */
fun newCalendar(day: Int = -1, month: Int = -1, year: Int = -1,
                hour: Int = -1, minute: Int = -1, second: Int = -1): Calendar {

  val calendar = Calendar.getInstance()
  calendar.clear()

  if (day != -1) {
    calendar.set(Calendar.DAY_OF_MONTH,  day)
  }

  if (month != -1) {
    calendar.set(Calendar.MONTH, month -1)
  }

  if (year != -1) {
    calendar.set(Calendar.YEAR, year)
  }

  if (hour != -1) {
    calendar.set(Calendar.HOUR_OF_DAY, hour)
  }

  if (minute != -1) {
    calendar.set(Calendar.MINUTE, minute)
  }

  if (second != -1) {
    calendar.set(Calendar.SECOND, second)
  }

  return calendar
}