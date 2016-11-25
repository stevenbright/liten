
import $ from 'jquery'; //var $ = require('jquery');
import {setUpPaginationHandlers} from './util/pagination';

// Set up global handlers
setUpPaginationHandlers();

$(document).ready(function () {
  $('#javascript-alert').remove();
  console.log('BTF loaded');
});
