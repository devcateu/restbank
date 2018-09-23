To build & run  project you need java 8 and maven

building project with tests: mvn clean install

to start application run:  java -jar target/restbank-1.0-SNAPSHOT.jar 

**First register account**

to register account you should send POST request 
to: http://localhost:4567/account/ 
with CONTENT-TYPE "application/json" 
and body like { "owner":"Chewbacca" }
all other action should be suggested in response by links

<br>

**curl sample:**

curl -X POST \
  http://localhost:4567/account/ \
  -H 'Content-Type: application/json' \
  -d '{	"owner":"Chewbacca" }'
  
**request body sample:**

<table>
<tr><td><b>relation</b> </td><td><b>sample body</b></td><td><b>Description</b></td></tr>
<tr><td>settle</td><td>{}</td><td>empty body, settle outgoing transaction</td></tr>
<tr><td>reject</td><td>{}</td><td>empty body, reject outgoing transaction</td></tr>
<tr><td>makeOutgoingTransaction</td><td>{ "destination":"43", "amount": "2" }</td><td>make outgoing transaction (from account); destination - destination account, amount -amount of transaction</td></tr>
<tr><td>receiveIncomingTransaction</td><td>{ "source":"43", "amount": "2" }</td><td>inform system about incoming transaction (to account); source - source account, amount -amount of transaction</td></tr>
</table>