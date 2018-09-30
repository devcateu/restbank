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

**Consideration about internal transaction**

In previous version of application I introduce special support for internal transaction. 
However, it introduces extra complexity and behaves not properly in the case of concurrent operation on destination account.
There are at least 3 options to resolve that problem:
<ol type="1">
<li>removing that functionality</li>
<li>introducing process manager, which monitor if particular action is done, algorithm below</li>
<li>introduce transactions, thanks that all events will be saved on the same time</li>
</ol>
I`ve selected option 1, because option 2 make code more complicated and option 3 could potentially does not work 
for some storages (current, in memory storage should be reimplemented for option 3). The advantages of option 1 were making my solution simpler and making all operation more consistent.  

<br/><br/>

Algorithm for process manager
<ol type="1">
    <li>save new process in db for Transaction(source, destination & amount) 
        and generated TransactionId for Incoming and Outgoing Transaction </li>
    <li>checking if outgoing transaction does not exist if not creating Outgoing transaction & save it</li>
    <li>updating process in db</li>
    <li>checking if incoming transaction does not exist if notcreating Incoming transaction</li>
    <li>updating process in db</li>
    <li>settling outgoing transaction if not yet settle</li>
    <li>removing row from db</li>
</ol>
Look that each step could fail. However introducing cron job allow to restart process on lastlty done business operation.