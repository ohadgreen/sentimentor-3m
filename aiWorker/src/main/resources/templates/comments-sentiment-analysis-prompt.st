your task is to analyze the sentiment towards {object} in the list of comments below, each with its comment id and text.
if the comment does not express a clear sentiment towards {object}, classify it as NEUTRAL.
These comments are regarding a Youtube video titled {videoTitle}
more information: {moreInfo}
Your response should be in JSON format with the following schema:
[commentId: (the original comment id),
sentiment: (POSITIVE, NEGATIVE, NEUTRAL),
sentimentReason: (short explanation for your judgement)]

Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
Do not include markdown code blocks in your response.
Remove the ```json markdown from the output.
Here are the comments:
{comments}