<#-- Contains the project description that will be put in the index page. -->
<p>
	FileConnector is an application connector that can be used to access files on the BCP local file system. 
	Operations include copying, moving and deleting files as well as reading and writing file contents.
</p>

<p>
	Methods include:
	<ul>
		<li><b>CopyFile</b> - Copies the given file on to the destination file. </li>
		<li><b>MoveFile</b> - Moves the given file on to the destination file. </li>	
		<li><b>SelectAndMoveFile</b> - Selects a file from the source directory based on the given criteria and moves it to the given destination directory. Source file is deleted after the operation.</li>	
		<li><b>DeleteFile</b> - Deletes the given file.</li>	
		<li><b>GetListOfFiles</b> - Returns a listing of all the files and directories in the given directory.</li>	
		<li><b>WriteFile</b> - Writes data to a text file.</li>	
		<li><b>ReadFile</b> - Reads data from a text file.</li>	
		<li><b>ReadFileRecords</b> - Reads data from a text file based on the configuration. File format is specified in a separate configuration XML file that is located in XMLStore.</li>	
		<li><b>ReadXmlFileRecords</b> - Reads XML structures from a valid XML file. Structures are selected with a simple XPath expression.</li>	
		<li><b>WriteFileRecords </b> - Writes data to a text file based on the configuration. File format is specified in a separate configuration XML file that is located in XMLStore.</li>	
		<li><b>CountNumberOfLines</b> - Counts the number of lines in the given text file.</li>	
	</ul>
</p>

<p>
	Currently there are 2 branches:
	<ul>
		<li>1.2.x - Latest version with support for file polling.</li>
		<li><a href="maintenance/1.1/index.html">1.1.x</a> - Maintenance version.</li>
	</ul> 
</p>

<p>
	Latest version is ${ant["buildinfo.latest.project.version"]}. 
</p>
	
<p>
	All available versions are here: <a href="versions.html">link</a>
</p>

<p>
	Project page in <a href="https://wiki.cordys.com/display/Con/File+Connector">CDN</a>.
</p>

