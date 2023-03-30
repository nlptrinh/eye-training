express =  require("express");
mongoose = require("mongoose");
logger = require("morgan");
const bodyParser = require("body-parser");
const short = require("short-uuid");


mongoose
  .connect(`mongodb+srv://pallab:pallab12345@cluster0.nbyjp93.mongodb.net/?retryWrites=true&w=majority`
  )
  .then(() => {
    
    console.log("database connected")
  })
  .catch((e) => {
    console.log(e)
  });

const app = express();
const port = 3000;

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(logger("dev"));

const options = { etag: false };
app.use(express.static("public", options));

const userSchema = new mongoose.Schema({
  userKey: String,
  ID: String,
  PW: String,
  sex: String,
  surgery: String,
  eyesight: String,
  lens: String,
  disorder: String,
});
const recordSchema = new mongoose.Schema({
  userKey: String,
  date: String,
  records: String,
});
const user = mongoose.model("user", userSchema);
const record = mongoose.model("record", recordSchema);

app.get("/post", (req, res) => {
  const target = { data: 0 };
  let saver = new user(target);
  saver.save((t) => {
    console.log("saved", target);
    res.send(t);
  });
});

app.post("/register", (req, res) => {
  console.log(req.body);
  const {body} = req;

  const uuid = short.generate();

  let saver = new user({ userKey: uuid });
  saver.save(() => {
    res.status(200).send({ userKey: uuid });
  });
});

app.post("/record/*", (req, res) => {
  console.log(req.body);
  const uuid = req.body.userKey;
  delete req.body["userKey"];
  const records = JSON.stringify(req.body);
  const date = new Date();

  let saver = new record({
    userKey: uuid,
    date: date,
    records: records,
  });

  saver.save(() => {
    res.status(200).send({});
  });
});

const clientURL = process.env.CLIENT_URL;

app.get("/get", (req, res) => {
  res.header("Access-Control-Allow-Origin", clientURL);

  user
    .find({})
    .then((result) => {
      res.send(result);
    })
    .catch((e) => {
      res.send(e);
    });
});

// app.listen(port, () => {
//   console.log(`server is on ${port}`);
// });

app.listen(3000, '0.0.0.0', function() {
  console.log(`Listening to port: ${port}` );
});