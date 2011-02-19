<#escape x as x?html>
<#assign current_page = "howtos">
<#include "include/header.ftl">
<#include "include/menu.ftl">

<div id="content">
	<h1>File Connector Examples</h1>
	
	<p>
		Available samples are:
		<ul>
			<li><a href="#reading-comma-separated">Reading a file with comma-separated values</a></li>
			<li><a href="#reading-fixed-length">Reading a file with fixed length fields</a></li>
		</ul>		
	</p>
	<p>	
		<a name="reading-comma-separated"><h2>Reading a file with comma-separated values</h2></a>
		<br/>
		<i>This sample is avaibale in docs/samples/reader-config.xml</i>
		<br/>
		Input file looks like this :
		<pre>
100;CSV Test;CSV Test Adress;
101;Name-101;Adress-101;
102;Name-101;Adress-102;
		</pre>
		
		Input SOAP request looks like this :	
		<pre>	
&lt;ReadFileRecords xmlns=&quot;http://schemas.cordys.com/1.0/ac/FileConnector&quot;&gt;
  &lt;filename&gt;c:\temp\read-csv-input.csv&lt;/filename&gt;
  &lt;filetype&gt;csv-sample&lt;/filetype&gt;
  &lt;numrecords&gt;-1&lt;/numrecords&gt;
  &lt;offset&gt;0&lt;/offset&gt;
  &lt;validateonly&gt;false&lt;/validateonly&gt;
&lt;/ReadFileRecords&gt;		
		</pre>
			
		Output SOAP response looks like this :
		<pre>	
&lt;ReadFileRecordsResponse xmlns=&quot;http://schemas.cordys.com/1.0/ac/FileConnector&quot;&gt;
  &lt;endoffset&gt;81&lt;/endoffset&gt;
  &lt;recordsread&gt;3&lt;/recordsread&gt;
  &lt;errorcount&gt;0&lt;/errorcount&gt;
  &lt;data&gt;
	&lt;tuple&gt;
	  &lt;line&gt;
		&lt;ID&gt;100&lt;/ID&gt;
		&lt;Name&gt;CSV Test&lt;/Name&gt;
		&lt;Address&gt;CSV Test Adress&lt;/Address&gt;
	  &lt;/line&gt;
	&lt;/tuple&gt;
	&lt;tuple&gt;
	  &lt;line&gt;
		&lt;ID&gt;101&lt;/ID&gt;
		&lt;Name&gt;Name-101&lt;/Name&gt;
		&lt;Address&gt;Adress-101&lt;/Address&gt;
	  &lt;/line&gt;
	&lt;/tuple&gt;
	&lt;tuple&gt;
	  &lt;line&gt;
		&lt;ID&gt;102&lt;/ID&gt;
		&lt;Name&gt;Name-101&lt;/Name&gt;
		&lt;Address&gt;Adress-102&lt;/Address&gt;
	  &lt;/line&gt;
	&lt;/tuple&gt;
  &lt;/data&gt;
&lt;/ReadFileRecordsResponse&gt;		
		</pre>
		
		ReadFileRecords method configuration XML looks like this:
		<pre>
&lt;filetype name=&quot;csv-sample&quot; recordsequence=&quot;line&quot; &gt;
	&lt;record name=&quot;line&quot; pattern=&quot;([^\n\r]*)[\n\r]+&quot; index=&quot;0&quot;&gt;
		&lt;field name=&quot;ID&quot; pattern=&quot;([^;]*);&quot; index=&quot;0&quot; /&gt;
		&lt;field name=&quot;Name&quot; pattern=&quot;([^;]*);&quot; index=&quot;0&quot; /&gt;
		&lt;field name=&quot;Address&quot; pattern=&quot;([^;]*);&quot; index=&quot;0&quot; /&gt;
	&lt;/record&gt;
&lt;/filetype&gt;
		</pre>	
	</p>
	
	<p>
		<a name="reading-fixed-length"><h2>Reading a file with fixed length fields</h2></a>
		<br/>
		<i>This sample is avaibale in docs/samples/reader-config.xml</i>
		<br/>
		<br/>
		In this sample fields a left-padded with spaces.
		<br/>
		Input file looks like this :
		<pre>
   100   Fixed Length Test      Fixed Length Test Adress
   101            Name-101                    Adress-101
   102            Name-102                    Adress-102
				
Field lengths are :
    6 |                20 |                          30 |
		</pre>
		
		Input SOAP request looks like this :	
		<pre>	
&lt;ReadFileRecords xmlns=&quot;http://schemas.cordys.com/1.0/ac/FileConnector&quot;&gt;
  &lt;filename&gt;c:\temp\read-fixedlength-input.txt&lt;/filename&gt;
  &lt;filetype&gt;fixedlength-sample&lt;/filetype&gt;
  &lt;numrecords&gt;-1&lt;/numrecords&gt;
  &lt;offset&gt;0&lt;/offset&gt;
  &lt;validateonly&gt;false&lt;/validateonly&gt;
&lt;/ReadFileRecords&gt;			
		</pre>

		Output SOAP response looks like this :
		<pre>	
&lt;ReadFileRecordsResponse xmlns=&quot;http://schemas.cordys.com/1.0/ac/FileConnector&quot;&gt;
  &lt;endoffset&gt;171&lt;/endoffset&gt;
  &lt;recordsread&gt;3&lt;/recordsread&gt;
  &lt;errorcount&gt;0&lt;/errorcount&gt;
  &lt;data&gt;
    &lt;tuple&gt;
      &lt;line&gt;
        &lt;ID&gt;100&lt;/ID&gt;
        &lt;Name&gt;Fixed Length Test&lt;/Name&gt;
        &lt;Address&gt;Fixed Length Test Adress&lt;/Address&gt;
      &lt;/line&gt;
    &lt;/tuple&gt;
    &lt;tuple&gt;
      &lt;line&gt;
        &lt;ID&gt;101&lt;/ID&gt;
        &lt;Name&gt;Name-101&lt;/Name&gt;
        &lt;Address&gt;Adress-101&lt;/Address&gt;
      &lt;/line&gt;
    &lt;/tuple&gt;
    &lt;tuple&gt;
      &lt;line&gt;
        &lt;ID&gt;102&lt;/ID&gt;
        &lt;Name&gt;Name-102&lt;/Name&gt;
        &lt;Address&gt;Adress-102&lt;/Address&gt;
      &lt;/line&gt;
    &lt;/tuple&gt;
  &lt;/data&gt;
&lt;/ReadFileRecordsResponse&gt;
		</pre>

		ReadFileRecords method configuration XML looks like this:
		<pre>	
&lt;filetype name=&quot;fixedlength-sample&quot; recordsequence=&quot;line&quot; &gt;
	&lt;record name=&quot;line&quot; pattern=&quot;([^\n\r]*)[\n\r]+&quot; index=&quot;0&quot;&gt;
		&lt;field name=&quot;ID&quot; pattern=&quot;\s*(\d+)&quot; index=&quot;0&quot; width=&quot;6&quot; /&gt;
		&lt;field name=&quot;Name&quot; pattern=&quot;\s*(.*)&quot; index=&quot;0&quot; width=&quot;20&quot; /&gt;
		&lt;field name=&quot;Address&quot; pattern=&quot;\s*(.*)&quot; index=&quot;0&quot; width=&quot;30&quot; /&gt;
	&lt;/record&gt;
&lt;/filetype&gt;			
		</pre>
	</p>
</div>
<#include "include/footer.ftl">
</#escape>