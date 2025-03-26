import { NetUtils } from 'net-utils';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    NetUtils.echo({ value: inputValue })
}
