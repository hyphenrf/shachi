# Gelbooru API Quirks

First of all, I should preface with my unambiguous gratitude that Gelbooru engine developers expose
a public API and allow bots like this app to access it freely. It just gives me so much grief that
sometimes just scraping the webpage seems so enticing. Grass probably just appears greener though.

[!NOTE]
> I use V1 and V2 in this document not to mean Gelbooru engine v0.2.x and v0.1.x, I didn't yet find
> a 0.1.x site with APIs enabled. What I'm talking about is a distinct change in response schemas.

## API V1 XML vs JSON and Quirks

On a zero-elements response, json mode returns literally nothing instead of an empty array, but as
`application/json` and without `content-length` header just because they're haters like that.

### Endpoint `s=post`

- json mode returns an array straight up, no `count` (derivable) or `offset` (go figure)
- json mode field name for `md5` is `hash`
- similarly: `has_comments:bool` ~? `comment_count:number`
- missing fields: `has_children`, `created_at`, `creator_id`
- extra fields: `image`, `directory`, `owner`, `sample`
- absence of a parent in `parent_id` is encoded as `""` in xml and `0` in json
- different ratings somehow?? same post can be both `q` (xml) and `general` (json)  
  Note: single-letter ratings seem to be deprecated (not present on newer boards responses).
  see [this xml response](https://safebooru.org/index.php?page=dapi&s=post&q=index&tags=md5%3Ae05b54a40fe120aefdfce5b2de5050bf)
  vs [this json response](https://safebooru.org/index.php?page=dapi&s=post&q=index&tags=md5%3Ae05b54a40fe120aefdfce5b2de5050bf&json=1)
  for example of rating type mismatch.

### Endpoint `s=comment`

- `json=1` has no effect
- usernames aren't escaped properly so the comments response can potentially be malformed xml  
  Example: `creator="Jeff\"ThePeanut\"Butterson"` is escaped as `\"` instead of `&quot;`

### Endpoint `s=tag`

- `json=1` has no effect

## API V2 XML vs JSON and Quirks

Sometimes it throws you an error as text/html and with response code 200 (despite promising: "You
should never receive an error unless the server is overloaded or the search dies")

### Endpoint `s=post`

- `has_notes`, `has_comments`, `has_children` are a `bool-as-string` in json

otherwise, this time around they nailed response consistency between formats. surprise! :)

### Endpoint `s=comment`

- V2 actually returns a V1 comments response so it's not even consistent with its own new schema.
  This means all V1 endpoint quirks still apply.

### Endpoint `s=tag`

- in the json response, tag names are xml-escaped for some reason. this is harmless.

## V1 to V2

Here I outline the differences in response schemas between versions.

### XML

It changed the response schema without versioning allowed in the requests, new schema uses nested
elements instead of attrs. No new attrs under those elements to make use of the change afaik, they
just felt cute and wanted to change things up?

One consistent observation seems to be that: Any API returning a V2 xml response supports `json=1`,
so if that's true, it means my app can just pretend the V2 xml doesn't exist.

- added `limit` to root attributes.
- default value for `score` and `parent_id` is now a number, `""` => `0`
- `directory`, `image`, `owner` and `sample:number-as-bool` added
- `title:string` & `post_locked:number-as-bool` introduced
- the `s=tag` fields didn't change from V1, just that they became child elements instead of attrs
- `ambiguous` in `s=tag` response: `boolean` => `number-as-bool` (note that there still are proper
  booleans in the `s=post` response)
- the `s=commwnt` response is V1-style as mentioned before, and its fields are unchanged.

### JSON

The json response similarly changed to follow the well-known convention:
`<posts ...><post>...</post>...</posts>` => `{"@attributes":{...},"post":[{...},...]}` instead of
returning a naked array.
