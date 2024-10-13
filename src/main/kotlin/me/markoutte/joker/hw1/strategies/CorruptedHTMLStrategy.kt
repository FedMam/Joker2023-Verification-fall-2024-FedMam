package me.markoutte.joker.hw1.strategies

import java.lang.reflect.Method
import java.nio.ByteBuffer

// Изменённый исходный код страницы https://acm.math.spbu.ru/
var legitHTML = """
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Programming Contests at Nsk State University</title>
    <link rel="stylesheet" href="/main.test.css?1"> 
    <script language="javascript">
      function add( type, y, m, d, num ) {
        document.write('<td><A HREF=/cgi-bin/monitor.pl/' + type + y + m + d + '.dat>' + num + '</A></td><td>20' + y + '.' + m + '.' + d + '</td>');
      }
    </script>
  </head>
  <body> 
    <h1>Programming&nbsp;Contests at<br />Nsk&nbsp;State&nbsp;University</h1>

    <h2>Welcome!</h2>
    <div>This site provides information on programming contests at the Nsk&nbsp;State&nbsp;University.</div>

    <h2>Congratulations</h2>
      <div>
        November 10, 2022:
        Nsk State University Team receives ICPC World Finals
        <span style="font-weight: bold; color: #c07070;">bronze medal</span>!
      </div>
      <div>
        October 5, 2021:
        Nsk State University Team takes 13th place in ICPC World Finals.
      </div>
      <!--
      <div>
        April 19, 2018:
        Nsk State University Team takes 14th place in ACM ICPC World Finals.
      </div>
      <div>
        May 24, 2017:
        Nsk State University Team receives ACM ICPC World Finals
        <span style="font-weight: bold; color: #c0c070;">gold medal</span>!
      </div>
      <div>
        May 19, 2016:
        Nsk State University Team becomes ACM ICPC World Finals
        <span style="font-weight: bold; color: red;">champion</span>!
      </div>
      -->

    <h2>Services</h2>

    <h3>Testing system</h3>
    <div>
      Our online testing system provides access to various training sessions.
      Use <a href="/tsweb/index.html">web client</a> to access it.
    </div>
    <!--<div>
      You cannot also explore our <a href="/tts">problem archive :(</a>.
    </div>-->

    <!--<h3>Upcoming contests</h3>
    <div>
      <span style="font-weight: bold; color: red;">May 22, 2016</span>: XIV School Cup
    </div>-->

    <h3>Recent contests</h3>
    <div>
      <span style="font-weight: bold;">October 15, 2023</span>: LVIII N SU Championship. <a href="/cgi-bin/monitor.pl/m231015.dat">Results</a> are available.
    </div>
    <div>
      <span style="font-weight: bold;">May 14, 2023</span>: LVII N SU Championship. <a href="/cgi-bin/monitor.pl/m230514.dat">Results</a> are available.
    </div>
    <!--
    <div>
      <span style="font-weight: bold;">September 15, 2019</span>: LIV N SU Championship. <a href="/cgi-bin/monitor.pl/n190915.dat">Results</a> available.
    </div>
    <div>
      <span style="font-weight: bold;">May 19, 2019</span>: XII School N SU Cup. <a href="/cgi-bin/monitor.pl/m190519.dat">Results</a> available.
    </div>
    <div>
      <span style="font-weight: bold;">March 11, 2019</span>: LIII N SU Championship. <a href="/cgi-bin/monitor.pl/n190421.dat">Results</a> available.
    </div>
    <div>
      <span style="font-weight: bold;">December 16, 2018</span>: LII N SU Championship. <a href="/cgi-bin/monitor.pl/n181216.dat">Results</a> available.
    </div>
    -->
    
    <h2>Past</h2>
      <div>
        <a href="/past.html">Past contest</a> results are available.
      </div>
      <div>
        <a href="/fame.html">Hall of fame</a>: explore our achievements.
      </div>

    <h2>Library</h2>

    <h2>Contact us</h2>
 
    <div>— VK <a href="https://vk.com/tsweb">community</a></div>
  </body>
</html>
"""

class CorruptedHTMLStrategy: FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 1000

    override fun generateString(buffer: ByteBuffer): String {
        val result = StringBuilder(legitHTML)

        val corruptions = buffer.get().toUByte().toInt() + buffer.get().toUByte().toInt()
        repeat(buffer.get().toUByte().toInt()) {
            val k = (buffer.get().toUByte().toInt() * 256 + buffer.get().toUByte().toInt()) % legitHTML.length
            result[k] = buffer.get().toUByte().toInt().toChar()
        }

        return result.toString()
    }
}