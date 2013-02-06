package paper

trait CleanUnicode {

  def cleanUnicode(s : String) : String = {
     s.replace("\u0001","")
      .replace("\u0002","")
      .replace("\u0003","")
      .replace("\u0004","")
      .replace("\u0005","")
      .replace("\u0006","")
      .replace("\u0007","")
      .replace("\u0008","")
      .replace("\u0009","")
      .replace("\u000a","")
      .replace("\u000b","")
      .replace("\u000c","")
      .replace("\u000d","")
      .replace("\u000e","")
      .replace("\u000f","")
      .replace("\u0010","")
      .replace("\u0011","")
      .replace("\u0012","")
      .replace("\u0013","")
      .replace("\u0014","")
      .replace("\u0015","")
      .replace("\u0016","")
      .replace("\u0017","")
      .replace("\u0018","")
      .replace("\u0019","")
      .replace("\u001a","")
      .replace("\u001b","")
      .replace("\u001c","")
      .replace("\u001d","")
      .replace("\u001e","")
      .replace("\u001f","")
      .replace("\uffff","")
  }
}
