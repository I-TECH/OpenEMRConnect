<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<html>

<head>

<title>Patient Request Form</title>
</head>

<body>

	<form:form method="POST" commandName="patientIdentification">

		<table>

			<tr>

				<td>Patient Identification:</td>
				<td><form:input path="identification" />
				</td>
			</tr>
			<tr>

				<td>Type of the Id</td>
				<td><form:select path="identificationType">

						<form:option value="0" label="Select" />

						<form:option value="ClinicalId" label="ClinicalId" />

						<form:option value="HDSS" label="HDSS" />


					</form:select>
				</td>
			</tr>
			<tr>

				<td>Source :</td>

				<td><form:radiobutton path="RequestSource" value="PIS"
						label="PIS" /> <form:radiobutton path="RequestSource" value="EMR"
						label="EMR" /></td>

			</tr>
			<tr>

				<td colspan="2"><input type="submit">
				</td>

			</tr>

		</table>

	</form:form>

</body>

</html>