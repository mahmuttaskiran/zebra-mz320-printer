package opcon.com.printer

/**
 * Created by aslitaskiran on 16/10/2017.
 */

private fun String.toen(): String {
  return (
      this.toUpperCase()
      .replace('Ç', 'C')
      .replace('ç', 'c')
      .replace('Ş', 'S')
      .replace('ş', 's')
      .replace('Ğ', 'G')
      .replace('ğ', 'g')
      .replace('İ', 'I')
      .replace('ı', 'i')
      .replace('Ö', 'O')
      .replace('Ü', 'U')
      )
}

fun String.appendSpace(count: Int): String {
  val builder = StringBuilder()
  builder.append(this)
  for (i in 1..count) builder.append(' ')
  return builder.toString()
}

private fun StringBuilder.appendNewLine(): StringBuilder {
  this.append("\r\n")
  return this
}

private fun String.completeToMax(max: Int): String {
  return this.appendSpace(max - this.length)
}

class CpclBuilder(val receipt: Receipt?) {

  fun buildPrintMessage(printMessage: String): String {
    val builder = StringBuilder()
    builder
        .append("! U1 SETLP 3 2 50").appendNewLine()
        .append(printMessage)
    return builder.toString()
  }

  fun buildTitle(): String {
    val builder = StringBuilder()
    builder.append("! 0 200 200 100 1").appendNewLine()
        .append("CENTER").appendNewLine()
        .append("TEXT 4 0 0 0 ").append(getString(R.string.best_title).toen()).appendNewLine()
        .append("TEXT 4 0 0 50 ").append(getString(R.string.best_title2).toen()).appendNewLine()
        .append("FORM").appendNewLine()
        .append("PRINT").appendNewLine()
    return builder.toString()
  }

  fun buildContent(): String {
    val builder = StringBuilder()
    builder
        .append("! U1 SETLP 3 2 50").appendNewLine()
        .append(getString(R.string.best_date).completeToMax(14)).append(": " + receipt?.getTimeAsText()).appendNewLine()
        .append(getString(R.string.best_name_and_surname).completeToMax(14)).append(": " + receipt?.customerName).appendNewLine()
        .append(getString(R.string.best_phone).completeToMax(14)).append(": " + receipt?.customerPhone).appendNewLine()
        .append(getString(R.string.bset_device).completeToMax(14)).append(": " + receipt?.getDevice()).appendNewLine()
        .append(getString(R.string.best_amount).completeToMax(14)).append(": " + receipt?.getAmount()).appendNewLine()
        .append(getString(R.string.best_task).completeToMax(14)).append(": " + receipt?.getTask()).appendNewLine()
        .append(getString(R.string.best_address).completeToMax(14)).append(": " + receipt?.getAddress()).appendNewLine()

    if (receipt?.hasNote()?: false) {
      builder.append(getString(R.string.best_notes).completeToMax(14)).append(": " + receipt?.getNote()).appendNewLine()
    }
    return builder.toString().toen()
  }

  fun buildCommonInfTitle(): String {
    val builder = StringBuilder()
    builder
        .append("! 0 200 200 140 1").appendNewLine()
        .append("CENTER").appendNewLine()
        .append("TEXT 5 2 0 50 ").append(getString(R.string.best_common_information_title).toen()).appendNewLine()
        .append("FORM").appendNewLine()
        .append("PRINT").appendNewLine()
    return builder.toString()
  }

  fun buildCommonInf(): String {
    val builder = StringBuilder()
        .append("! U1 SETLP 3 1 50").appendNewLine()
        .append(getBestTitle("best_common_information")?.toen())
    return builder.toString().toen()
  }

  fun buildCommonWarrentyTitle(): String {
    val builder = StringBuilder()
    builder.append("! 0 200 200 140 1").appendNewLine()
        .append("CENTER").appendNewLine()
        .append("TEXT 5 2 0 50 ").append(getString(R.string.best_warranty_title).toen()).appendNewLine()
        .append("FORM").appendNewLine()
        .append("PRINT").appendNewLine()
    return builder.toString()
  }

  fun buildCommonWarrenty(): String {
    val builder = StringBuilder()
    val strs = getBestTitle("best_warranty")
    getStringNewLinedArray(strs).forEachIndexed { index, it ->
      builder.append(it)
      if (strs != null && index < strs.length -1) {
        builder.appendNewLine()
      }
    }
    return builder.toString().toen()
  }

  fun buildCommunicationTitle(): String {
    val builder = StringBuilder()
    builder.append("! 0 200 200 140 1").appendNewLine()
        .append("CENTER").appendNewLine()
        .append("TEXT 5 2 0 50 ").append(getString(R.string.best_communication_title).toen()).appendNewLine()
        .append("FORM").appendNewLine()
        .append("PRINT").appendNewLine()
    return builder.toString()
  }

  fun buildCommunication(): String {
    val builder = StringBuilder()
    builder.append("! U1 SETLP 4 0 50").appendNewLine()
    val strs = getBestTitle("best_communication")
    getStringNewLinedArray(strs).forEachIndexed { index, it ->
      builder.append(it)
      if (strs != null && index < strs.length -1) {
        builder.appendNewLine()
      }
    }
    return builder.toString().toen()
  }

  fun buildSignificant(): String {
    val builder = StringBuilder()
    builder.append("! 0 200 200 150 1").appendNewLine()
        .append("LEFT").appendNewLine()
        .append("TEXT 0 2 0 0 ").append(getString(R.string.best_staff_significant).toen()).appendNewLine()
        .append("LEFT").appendNewLine()
        .append("TEXT 0 2 0 40 ").append(receipt?.getStaff()?.toen()).appendNewLine()
        .append("RIGHT").appendNewLine()
        .append("TEXT 0 2 0 0 ").append(getString(R.string.best_customer_significant).toen()).appendNewLine()
        .append("FORM").appendNewLine()
        .append("PRINT").appendNewLine()
    return builder.toString()
  }

  fun getStringNewLinedArray(str: String?): List<String>{
    return str?.split("\n")?: listOf()
  }

}


