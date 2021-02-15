from __future__ import print_function
import time
import sib_api_v3_sdk
# from sib_api_v3_sdk.rest import Sm
from pprint import pprint
from releaseraccoon.settings import sendinblue_api_key

from sib_api_v3_sdk.rest import ApiException


configuration = sib_api_v3_sdk.Configuration()
configuration.api_key['api-key'] = sendinblue_api_key

api_instance = sib_api_v3_sdk.TransactionalEmailsApi(sib_api_v3_sdk.ApiClient(configuration))
send_smtp_email = sib_api_v3_sdk.SendSmtpEmail(
    to=['johnaivalis@gmail.com'],
    text_content='Your first email',
    subject='Release Raccoon has something to say',
    reply_to='releaseraccoon@gmail.com'
)  # SendSmtpEmail | Values to send a transactional email

try:
    # Send a transactional email
    api_response = api_instance.send_transac_email(send_smtp_email)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling TransactionalEmailsApi->send_transac_email: %s\n" % e)
